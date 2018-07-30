# spring-netflix-oss

Teste das configurações de timeout e retry do hystrix e ribbon.

O serviço `hello` chama o serviço `echo-server` passando:

- `id` para identificação da chamada;
- `duration` para simular a latência do serviço;
- `retries` que indica a quantidade de vezes que deve retentar a chamada.

O `duration` deve ser maior que a metade e menor que o total do timeout, assim habilita a regra para realizar o retry, pois são contabilizadas quantas vezes o mesmo id é chamado, se for igual ao número de retries retorna `duration`, senão retorna `duration * 2`

### build

```
./gradlew build
```

### rodar em 2 terminais diferentes:

```
java -jar hello/build/libs/hello-1.0.0.jar

```

```
java -jar echo-server/build/libs/echo-server-1.0.0.jar

```

### exemplo de chamadas usando https://httpie.org/:

Sem retry:

```
$ time http :8081/hello id==0 duration==5100 retries==0
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Mon, 30 Jul 2018 19:48:05 GMT
Transfer-Encoding: chunked

{
    "calls": 1,
    "id": 0
}


real	0m5.573s
user	0m0.201s
sys	0m0.028s
```

Com 1 retry:

```
$ time http :8081/hello id==1 duration==5100 retries==1
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Mon, 30 Jul 2018 19:42:12 GMT
Transfer-Encoding: chunked

{
    "calls": 2,
    "id": 1
}


real	0m15.443s
user	0m0.200s
sys	0m0.029s
```

Com tentativa de 2 retries, mas estoura o timeout:

```
$ time http :8081/hello id==2 duration==5100 retries==2
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Mon, 30 Jul 2018 19:44:42 GMT
Transfer-Encoding: chunked

{
    "calls": 0,
    "id": 2
}


real	0m20.283s
user	0m0.238s
sys	0m0.012s
```

Log do echo-server:

```
[0]: GET /echo?id=0,duration=5100,retries=0
[0]: current: 0, retries: 0
[0]: {0=1}
[0]: Waiting for 5100 ms
[1]: GET /echo?id=1,duration=5100,retries=1
[1]: current: 0, retries: 1
[1]: {0=1, 1=1}
[1]: Waiting for 10200 ms
[1]: GET /echo?id=1,duration=5100,retries=1
[1]: current: 1, retries: 1
[1]: {0=1, 1=2}
[1]: Waiting for 5100 ms
[2]: GET /echo?id=2,duration=5100,retries=2
[2]: current: 0, retries: 2
[2]: {0=1, 1=2, 2=1}
[2]: Waiting for 10200 ms
[2]: GET /echo?id=2,duration=5100,retries=2
[2]: current: 1, retries: 2
[2]: {0=1, 1=2, 2=2}
[2]: Waiting for 10200 ms
```

### configuração

Para os testes o timeout de uma requisição é 10000 ms e apenas 1 retry, seguindo a fórmula `timeoutInMilliseconds = ReadTimeout * MaxAutoRetries + ConnectTimeout`:

```
echo-server:
  ribbon:
    ReadTimeout: 10000
    ConnectTimeout: 1000
    MaxAutoRetries: 1
    MaxAutoRetriesNextServer: 0
    listOfServers: localhost:8080
feign:
  hystrix:
    enabled: true
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 21000
```
