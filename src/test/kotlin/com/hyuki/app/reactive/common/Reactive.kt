package com.hyuki.app.reactive.common

import com.hyuki.app.reactive.ReactiveApplicationTests
import org.junit.jupiter.api.Test
import org.reactivestreams.Subscription
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

class Reactive: ReactiveApplicationTests()  {

    @Test
    fun pub() {
        //Publisher -> Flux, Mono

        val block = Flux.range(1, 100) //1부터 100까지의 정수 시퀀스 생성
//            .repeat() //소스 스트림이 끝나고 해당 스트림을 다시 구독한다.
            .collectList() //생산된 모든 요소를 단일 리스트로 만든다.
            .block() //실제 구독을 기동하고 최종 결과가 도착 할때까지 실행 중인 스레드 차단.

        println(block)

        //CompletableFuture는 Mono와 달리 반드시 반환 값을 반환해야 한다.
        //또한 CompletableFuture는 즉시 처리를 시작하고 Mono는 구독자가 나타날 때 까지 아무 작업도 수행하지 않는다.
        //Mono 타입의 이점은 리액티브 연산자를 많이 제공한다.

        //Mono와 flux는 서로 유연하게 변경 가능 하다.
        val mono = Mono.just("1")
        Mono.from(Flux.from(mono))

        //Flowable은 Flux와 비슷한 역할이지만 Publisher유형의 인수를 사용할 수 있다.
        //Single -> 정확히 하나의 요소를 생성하는 스트림, Mono보단 Completable과 더 비슷하지만 구독이 발생했을때만 처리한다.
        //Maybe -> Mono와 비슷하다. Publisher 인터페이스를 구현하지 않아서 리액티브 스트림과 호환성이 없다. toFlowable메서드 제공
        //Maybe<T> = Mono<T> , Mono<Void> = Completable

        val stream1 = Flux.just("Hello", "World")
        val stream2 = Flux.fromArray(arrayOf(1,2,3))
        val stream3 = Flux.fromIterable(listOf(1,2,3))
        val stream4 = Flux.range(1,100)


    }

    @Test
    fun sub() {
        Flux.just("A", "B", "C")
            .subscribe(
                { data -> println(data) }, //Consumer<T> DataConsumer
                { err -> run { err.printStackTrace() } }, //Consumer<Throwable> errConsumer
                { println("onComplete") } //Runnable CompleteConsumer
            )


        Flux.range(1, 100)
            .subscribe(
                { data -> println("onNext: $data") }, //Consumer<T> DataConsumer
                { err -> run { err.printStackTrace() } }, //Consumer<Throwable> errConsumer
                { println("onComplete") }, //Runnable CompleteConsumer
                { t: Subscription -> run {t.request(4); t.cancel() } } //subscription 제어 ( 이런식으로 직접 제어는 하면 안됨 )
            )
    }

    @Test
    fun index() {
        Flux.range(2018, 5) // Flux<Integer>
            .timestamp() // Flux<Tuple2<Long, Integer>>
            .index() // Flux<Tuple2<Long, Tuple2<Long, Integer>>>
            .subscribe { e -> println("index = ${e.t1}, ts = ${Instant.ofEpochMilli(e.t2.t1)}, value = ${e.t2.t2}") }
    }

    @Test
    fun evenFilter() {
        Flux.range(1, 100)
            .filter { t -> t % 2 == 0 }
            .log()
            .collectList()
            .subscribe { e -> println(e.size) }
    }

    @Test
    fun thenMany() {
        Flux.range(1, 100)
            .filter { t -> t % 2 == 0 }
            .log()
            .thenMany(Flux.just(10, 3)) //상위 스트림은 무시 된다.
            .log()
            .collectList()
            .subscribe { e -> println(e.size) }
    }

    @Test
    fun concat() {
        Flux.concat(
            Flux.range(1, 3),
            Flux.range(4, 3),
            Flux.range(7,4)
        ).log()
         .subscribe (
            { data -> println(data) },
            { err -> run { err.printStackTrace() } },
            { println("onComplete")}
         )
    }

    @Test
    fun merge() {
        Flux.merge(
            Flux.range(1, 3),
            Flux.range(4, 3),
            Flux.range(7,4)
        ).log()
            .subscribe (
                { data -> println(data) },
                { err -> run { err.printStackTrace() } },
                { println("onComplete")}
            )
    }

    @Test
    fun buffer() {
        Flux.range(1, 100)
            .buffer(10)
            .subscribe{ data -> println(data)}
    }

    @Test
    fun window() {
        val windowedFlux = Flux.range(101, 20)
            .windowUntil({ it % 5 == 0 }, true)

        windowedFlux.subscribe { windowedFlux ->
            windowedFlux
                .log()
                .collectList()
                .subscribe { data -> println(data) }
        }
    }



}