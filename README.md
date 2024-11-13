# Kotlin Spring OpenAPI Generator

Gradle plugin to help you interact with the Jaqpot API using Java/Kotlin.

## Installation

Add this dependency to your project:

Gradle (Kotlin DSL)

```kotlin
id("org.jaqpot.kotlin-sdk") version "0.2.0"
```

Gradle (Groovy)

```groovy
id 'org.jaqpot.kotlin-sdk' version '0.2.0'
``` 

## Usage

In java code, you can use the generated client like this:

```java
ModelApiClient modelApiClient = new ModelApiClient(System.getenv("JAQPOT_API_KEY"), System.getenv("JAQPOT_API_SECRET"));
System.out.

        Dataset dataset = modelApiClient
        .predictSync(
                modelId,
                List.of(
                        Map.of("X1", "1", "X2", "2", "X3", "3", "X4", "4")
                )
        );
```

In Kotlin:

```kotlin
val modelApiClient = ModelApiClient(System.getenv("JAQPOT_API_KEY"), System.getenv("JAQPOT_API_SECRET"))
val dataset = modelApiClient.predictSync(
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
