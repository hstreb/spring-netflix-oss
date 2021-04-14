# spring-netflix-oss

Teste das configurações de timeout e retry do hystrix e ribbon.

O serviço `hello` chama o serviço `echo-server` passando:

- `id` para identificação da chamada;
- `duration` para simular a latência do serviço;
- `retries` que indica a quantidade de vezes que deve retentar a chamada.

O `duration` deve ser maior que a metade e menor que o total do timeout, assim habilita a regra para realizar o retry, pois são contabilizadas quantas vezes o mesmo id é chamado, se for igual ao número de retries retorna `duration`, senão retorna `duration * 2`

### build

```sh
./gradlew build
```

### baixar opentelemetry agent

```
wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent-all.jar
```

### baixar opentelemetry agent

```
wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.21.0/elastic-apm-agent-1.21.0.jar
```

### rodar com agente opentelemetry enviando para ELK APM

```
java -javaagent:opentelemetry-javaagent-all.jar \
  -Dotel.exporter.otlp.endpoint=http://localhost:8200 \
  -Dotel.metrics.exporter=none \
  -Dotel.resource.attributes=service.name=hello,deployment.environment=production \
  -jar hello/build/libs/hello-1.0.0.jar
```

```
java -javaagent:opentelemetry-javaagent-all.jar \
  -Dotel.exporter.otlp.endpoint=http://localhost:8200 \
  -Dotel.metrics.exporter=none \
  -Dotel.resource.attributes=service.name=echo-server,deployment.environment=production \
  -jar echo-server/build/libs/echo-server-1.0.0.jar
```

### rodar com agente ELK APM

```
java -javaagent:./elastic-apm-agent-1.21.0.jar \
     -Delastic.apm.service_name=hello \
     -Delastic.apm.server_urls=http://localhost:8200 \
     -Delastic.apm.secret_token= \
     -Delastic.apm.environment=production \
     -Delastic.apm.enable_log_correlation=true \
     -Delastic.apm.application_packages=org.example \
     -jar hello/build/libs/hello-1.0.0.jar
```

```
java -javaagent:./elastic-apm-agent-1.21.0.jar \
   -Delastic.apm.service_name=echo-server \
   -Delastic.apm.server_urls=http://localhost:8200 \
   -Delastic.apm.secret_token= \
   -Delastic.apm.environment=production \
   -Delastic.apm.enable_log_correlation=true \
   -Delastic.apm.application_packages=org.example \
   -jar echo-server/build/libs/echo-server-1.0.0.jar
```

### rodar testes usando https://httpie.org/:

Para cada configuração acima rodar a bateria de testes abaixo:

Em um terminal rodar 500 requisições sequenciais

```
for i in $(seq 0 500); do time http :8081/hello id==$i duration==0 retries==0; done
```

Em outro terminal rodar 500 requisições sequenciais

```
for i in $(seq 0 500); do time http :8081/hello id==$i duration==0 retries==0; done
```

### rodar com agente opentelemetry enviando para Jaeger:

```
java -javaagent:opentelemetry-javaagent-all.jar \
  -Dotel.traces.exporter=jaeger \
  -Dotel.metrics.exporter=none \
  -Dotel.resource.attributes=service.name=hello \
  -jar hello/build/libs/hello-1.0.0.jar
```

```
java -javaagent:opentelemetry-javaagent-all.jar \
  -Dotel.traces.exporter=jaeger \
  -Dotel.metrics.exporter=none \
  -Dotel.resource.attributes=service.name=echo-server,deployment.environment=production \
  -jar echo-server/build/libs/echo-server-1.0.0.jar
```