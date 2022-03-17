package dev.alraj.simplecalladaptersample

import com.google.gson.annotations.SerializedName

data class Hello(@SerializedName("result") val result: String)