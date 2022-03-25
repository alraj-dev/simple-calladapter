package dev.alraj.simplecalladaptersample

import com.google.gson.annotations.SerializedName

data class Base<T>(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val `data`: T?
)

data class Software(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String
)

data class Cat(
    @SerializedName("species")
    val species: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("gender")
    val gender: String
)

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("age")
    val age: Int
)

typealias UserResponse = Base<User>