/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.util;

import dev.ultreon.qvoxel.resource.ThrowingSupplier;
import org.jetbrains.annotations.Contract;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <T>
 * @author <a href="https://github.com/XyperCodee">Qubilux</a>
 * @since 0.2.0
 */
public class Result<T> {
    private final Ok<T> ok;
    private final Failure failure;

    private Result(Ok<T> ok, Failure failure) {
        this.ok = ok;
        this.failure = failure;
    }

    public static <T> Result<T> ok(T left) {
        return new Result<>(new Ok<>(left), null);
    }

    public static Result<Void> ok() {
        return new Result<>(new Ok<>(null), null);
    }

    public static <T> Result<T> ofNullable(T value) {
        if (value == null) {
            return failure(new NullPointerException("Value is null"));
        }
        return ok(value);
    }

    public static Result<Void> runCatching(Runnable runnable) {
        try {
            runnable.run();
            return ok(null);
        } catch (Throwable t) {
            return failure(t);
        }
    }

    public static <T> Result<T> supplyCatching(Supplier<T> supplier) {
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return failure(t);
        }
    }

    public static <T> Result<T> supplyCatching(ThrowingSupplier<T, ?> supplier) {
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return failure(t);
        }
    }

    public static <T> Result<T> failure(Throwable right) {
        return new Result<>(null, new Failure(right));
    }

    public T getOk() {
        if (ok == null) throw new NoSuchElementException("The value is not present.");
        return ok.value;
    }

    public Throwable getFailure() {
        if (failure == null) throw new NoSuchElementException("The failure is not present.");
        return failure.throwable;
    }

    public boolean isOk() {
        return ok != null;
    }

    public boolean isFailure() {
        return failure != null;
    }

    public void ifValue(Consumer<T> onValue) {
        if (ok != null) onValue.accept(ok.value);
    }

    public void ifFailure(Consumer<Throwable> onFailure) {
        if (failure != null) onFailure.accept(failure.throwable);
    }

    public void ifValueOrElse(Consumer<T> onValue, Runnable runnable) {
        if (ok != null) onValue.accept(ok.value);
        else runnable.run();
    }

    public void ifFailureOrElse(Consumer<Throwable> onFailure, Runnable runnable) {
        if (failure != null) onFailure.accept(failure.throwable);
        else runnable.run();
    }

    public T getValueOrNull() {
        if (ok == null) return null;
        return ok.value;
    }

    public Throwable getFailureOrNull() {
        if (failure == null) return null;
        return failure.throwable;
    }

    @Contract("!null -> !null")
    public T getValueOr(T other) {
        Ok<T> ok = this.ok;
        if (ok == null) return other;
        T value = ok.value;
        return value == null ? other : value;
    }

    public Throwable getFailureOr(Throwable other) {
        Failure failure = this.failure;
        if (failure == null) return other;
        Throwable value = failure.throwable;
        return value == null ? other : value;
    }

    public T getValueOrGet(Supplier<? extends T> other) {
        Ok<T> ok = this.ok;
        if (ok == null) return other.get();
        T value = ok.value;
        return value == null ? other.get() : value;
    }

    public Throwable getFailureOrGet(Supplier<? extends Throwable> other) {
        Failure failure = this.failure;
        if (failure == null) return other.get();
        Throwable value = failure.throwable;
        return value == null ? other.get() : value;
    }

    public void ifAny(Consumer<T> onValue, Consumer<Throwable> onFailure) {
        if (ok != null) onValue.accept(ok.value);
        else if (failure != null) onFailure.accept(failure.throwable);
    }

    public T unwrap() {
        if (failure != null) throw new NoSuchElementException("The failure is not present.");
        if (ok == null) throw new NoSuchElementException("The value is not present.");
        return ok.value;
    }

    public Throwable unwrapFailure() {
        if (failure == null) throw new NoSuchElementException("The failure is not present.");
        return failure.throwable;
    }

    public T unwrapOr(T other) {
        Ok<T> ok = this.ok;
        if (ok == null) return other;
        T value = ok.value;
        return value == null ? other : value;
    }

    public Throwable unwrapOrGet(Supplier<? extends Throwable> other) {
        Failure failure = this.failure;
        if (failure == null) return other.get();
        Throwable value = failure.throwable;
        return value == null ? other.get() : value;
    }

    public T expect(String message) {
        if (ok == null) throw new NoSuchElementException(message);
        return ok.value;
    }

    public Throwable expectFailure(String message) {
        if (failure == null) throw new NoSuchElementException(message);
        return failure.throwable;
    }

    public <R> Result<R> map(Function<T, R> mapper, Function<Throwable, Throwable> mapperFailure) {
        if (ok != null) return ok(mapper.apply(ok.value));
        if (failure != null) return failure(mapperFailure.apply(failure.throwable));
        throw new Error("Unreachable code.");
    }

    public <R> R flatMap(Function<T, R> mapper, Function<Throwable, R> mapperFailure) {
        if (ok != null) return mapper.apply(ok.value);
        if (failure != null) return mapperFailure.apply(failure.throwable);
        throw new Error("Unreachable code.");
    }

    private static class Ok<L> {
        private final L value;

        public Ok(L value) {
            this.value = value;
        }
    }

    private static class Failure {
        private final Throwable throwable;

        public Failure(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
