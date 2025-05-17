# SuperController

SuperController is a Kotlin library designed to simplify the creation of CRUD (Create, Read, Update, Delete) operations in Spring Boot applications. It provides base classes `SuperController` and `SuperService` that can be extended to quickly implement standardized CRUD functionality.

## Motivation

The primary goal of this project is to streamline the process of creating CRUD operations by extending the `SuperController` and `SuperService` classes. This approach reduces boilerplate code and promotes consistency across different parts of your application.

## Features

- Easy CRUD implementation by extending `SuperController` and `SuperService`
- Customizable request/response mapping
- Built-in support for pagination
- Flexible policy-based authorization
- Support for custom filters in services

## Getting Started

### Prerequisites

- Kotlin 1.5+
- Spring Boot 2.x or 3.x

### Installation

Add the following dependency to your `build.gradle.kts` file:

```kotlin
implementation("io.github.robertomike:super-controller:$version")
