package mods.betterfoliage.util

import net.minecraft.client.Minecraft
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer
import java.util.function.Function

fun completedVoid() = CompletableFuture.completedFuture<Void>(null)!!

fun <T, U> CompletionStage<T>.map(func: (T)->U) = thenApply(Function(func)).toCompletableFuture()!!
fun <T, U> CompletionStage<T>.mapAsync(func: (T)->U) = thenApplyAsync(Function(func), Minecraft.getInstance()).toCompletableFuture()!!

fun <T> CompletionStage<T>.sink(func: (T)->Unit) = thenAccept(Consumer(func)).toCompletableFuture()!!
fun <T> CompletionStage<T>.sinkAsync(func: (T)->Unit) = thenAcceptAsync(Consumer(func), Minecraft.getInstance()).toCompletableFuture()!!