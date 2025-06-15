package io.github.robertomike.super_controller

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class SuperCache(
    val value: String, // Principal key of the cache name, example: "users"
    val prefix: String = "", // Prefix of the cache key specific, example: "user:1" where 1 is the id
    val saveIndex: Boolean = true,
    val saveSingle: Boolean = true,
    val saveOnStore: Boolean = false,
)
