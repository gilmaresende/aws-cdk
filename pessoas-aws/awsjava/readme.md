```
version: "3.8"

services:
  mysql-comum:
    image: mysql:8.3.0
    container_name: mysql-comum
    environment:
      MYSQL_ROOT_PASSWORD: my_password_root # Senha do root
      MYSQL_DATABASE: db_super # Banco de dados padrão
      MYSQL_USER: user_n1 # Usuário adicional
      MYSQL_PASSWORD: user_n1 # Senha do usuário adicional
    ports:
      - "3306:3306" # Porta mapeada para acesso ao MySQL
    volumes:
      - mysql_comum:/var/lib/mysql # Volume persistente para os dados do banco
    command: >
      --default-authentication-plugin=mysql_native_password
      --bind-address=0.0.0.0

volumes:
  mysql_comum:

```

Gerar imagem

```
mvn spring-boot:build-image
```

Enviar imagem

```
docker push gilmaresende/awsjava:0.0.3
```

cdk deploy --parameters Rds:databasePassword=sdaSFGkldf483fd Rds Service01 VPC Cluster
