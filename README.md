# Java Retry

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.rbuck/java-retry/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.rbuck/java-retry)

[<img src="https://travis-ci.org/rbuck/java-retry.svg?branch=master" alt="Build Status" />](http://travis-ci.org/rbuck/java-retry)

Lets developers make their applications more resilient by adding robust
transient fault handling logic. Transient faults are errors that occur
because of some temporary condition such as network connectivity issues
or service unavailability. Typically, if you retry the operation that
resulted in a transient error a short time later, you find that the
error has disappeared.

## Retry

Infrastructure exists to facilitate automatic transaction retry, and exception
detectors are written to properly handle a variety of SQLException types, so that
retry occurs during transient exceptions, not others.

Retry has been implemented in a generic fashion so as to be pluggable in a number
of other non-SQL enterprise scenarios; so long as an exception detector is
appropriately written, business level activities can be written in a fault
tolerant manner.

### Generic Retry Example

Here is an example of your basic non-SQL retry loop:

```java
// the detector may optionally be a singleton...
TransientExceptionDetector detector = new TransientExceptionDetector() {
    @Override
    public boolean isTransient(Exception e) {
        // check exception type or content...
    }
};
// the retry policy and strategies are allocated per transaction
// and cannot be singletons, are typed...
RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new ExponentialBackoff(), detector);
int result = 0;
try {
    result = retryPolicy.action(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return 5;
        }
    });
} catch (Exception e) {
    // ... do something to handle or log ...
}
```

There are a few actors on stage here:

- the transient exception detector (returns true if the exception is a transient)
- the retry policy (consistently applied policy)
- the retry strategy (provides pluggable retry behaviors)
- the callable (the action to be executed with retry capabilities)

### SQL Retry Example

Here is an example of your basic SQL retry loop:

```java
SqlTransactionContext sqlTransactionContext = new ...
SqlRetryPolicy<Integer> sqlRetryPolicy = new SqlRetryPolicy<>(
    new FixedInterval(1, 100), sqlTransactionContext);
try {
    result = sqlRetryPolicy.action(new SqlCallable<Integer>() {
        @Override
        public Integer call(Connection connection) throws SQLException {
            int result = ... from SQL result set ...
            // critical: make sure to use try-with-resources to
            // properly close all statements and result sets! 
            return result;
        }
    });
} catch (Exception e) {
    // ... do something to handle or log ... 
}
```

### Spring Integration

The following Spring Bean definition snippet was used in a Mule ESB
flow to enforce comprehensive resiliency for a subscriptions service.

```xml
	<spring:beans>
		<spring:bean id="hikariConfig" class="com.zaxxer.hikari.HikariConfig">
			<spring:property name="driverClassName" value="com.nuodb.jdbc.Driver" />
			<spring:property name="jdbcUrl"
				value="jdbc:com.nuodb://localhost/subscribers?schema=subs" />
			<spring:property name="username" value="dba" />
			<spring:property name="password" value="dba" />
			<spring:property name="autoCommit" value="true" />
			<spring:property name="readOnly" value="false" />
			<spring:property name="connectionTestQuery" value="SELECT 1 FROM DUAL" />
			<spring:property name="maximumPoolSize" value="100" />
			<spring:property name="maxLifetime" value="120000" />
			<spring:property name="isolateInternalQueries" value="true" />
			<spring:property name="transactionIsolation" value="TRANSACTION_READ_COMMITTED" />
		</spring:bean>

		<spring:bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
			<spring:constructor-arg ref="hikariConfig" />
		</spring:bean>

		<!--
		Wraps a connection provider, in this case simply a DataSource.
		-->
		<spring:bean id="transactionContext"
			class="com.github.rbuck.retry.BasicSqlTransactionContext">
			<spring:constructor-arg ref="dataSource" />
		</spring:bean>

		<!--
		Default of 10 retries at most, binary exponential back-off,
		starts at 1 second, progresses to 10 seconds each. 
		-->
		<spring:bean id="retryStrategy"
			class="com.github.rbuck.retry.ExponentialBackoff" />

		<!-- 
		The retry policy implements the retry strategy for SQL transactions
		within the subscriptions service.
		-->
		<spring:bean id="sqlRetryPolicy"
			class="com.github.rbuck.retry.SqlRetryPolicy">
			<spring:constructor-arg ref="retryStrategy" />
			<spring:constructor-arg ref="transactionContext" />
		</spring:bean>

		<spring:bean id="SubscriberBean" class="com.nuodb.samples.mule.SubscriberImpl">
			<spring:property name="retryPolicy">
				<spring:ref local="sqlRetryPolicy" />
			</spring:property>
		</spring:bean>
	</spring:beans>
```

## Building and Releasing

To compile and test the project issue the following commands:

    mvn clean install

To release the project issue the following commands:

    mvn release:clean
    mvn release:prepare
    mvn release:perform

## Linking

This project has been released to Maven Central; to use it simply include this
in your Maven POM file:

    <dependency>
        <groupId>com.github.rbuck</groupId>
        <artifactId>java-retry</artifactId>
        <version>1.1</version>
        <scope>compile</scope>
    </dependency>

## License

This project is Apache 2.0 licensed.
