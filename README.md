# Jaqpot Kotlin/Java SDK

Kotlin library to help you interact with the [Jaqpot API](https://jaqpot.org/docs/jaqpot-api) using Java/Kotlin.

## Installation

Add this dependency to your project:

Gradle (Kotlin DSL)

```kotlin
implementation("org.jaqpot:kotlin-sdk:0.4.0")
```

Maven

```maven
<dependency>
    <groupId>org.jaqpot</groupId>
    <artifactId>kotlin-sdk</artifactId>
    <version>0.4.0</version>
</dependency>
``` 

## Usage

To use the SDK you'll need to generate the Jaqpot API keys, following the
guide [here](https://jaqpot.org/docs/jaqpot-api/authentication/create-an-api-key)

In Java, you can use the generated client like this:

```java
JaqpotApiClient jaqpotApiClient = new JaqpotApiClient(System.getenv("JAQPOT_API_KEY"), System.getenv("JAQPOT_API_SECRET"));
Dataset dataset = jaqpotApiClient
        .predictSync(
                modelId,
                List.of(
                        Map.of("X1", "1", "X2", "2", "X3", "3", "X4", "4")
                )
        );
System.out.println(dataset)
```

or in Kotlin:

```kotlin
val jaqpotApiClient = JaqpotApiClient(System.getenv("JAQPOT_API_KEY"), System.getenv("JAQPOT_API_SECRET"))
val dataset = jaqpotApiClient.predictSync(
    modelId,
    listOf(
        mapOf("X1" to "1", "X2" to "2", "X3" to "3", "X4" to "4")
    )
)
println(dataset)
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

## License

MIT
