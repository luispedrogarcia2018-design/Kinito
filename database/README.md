# Cómo ejecutar

Orden de ejecución (cada archivo depende del anterior):

1. Primero Instalar MySQL
```bash
sudo apt update
sudo apt install mysql-server
```

2. Configurar contraseña en el root
```bash
sudo mysql
sudo apt install mysql-server
```

Dentro de MySQL
```bash
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'TuPasswordPropia';
FLUSH PRIVILEGES;
EXIT;
```

3. Clonar repo de github
```bash
git clone <la-URL-de-tu-repo>
cd <nombre-del-proyecto>
```

4. Crear base de datos con scripts en orden
```bash
cd database
mysql -u root -p < 01_database.sql
mysql -u root -p < 02_tables.sql
mysql -u root -p < 03_indexes.sql
mysql -u root -p < 04_views.sql
mysql -u root -p < 05_seed_data.sql
mysql -u root -p < 06_test_queries.sql   # opcional: consultas de verificación/reportes
```

5. Crear tu propio .vscode/launch.json
```bash
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "TiendaApplication",
      "request": "launch",
      "mainClass": "com.tienda.ropa.TiendaApplication",
      "projectName": "ropa",
      "env": {
        "DB_PASSWORD": "TuPasswordPropia"
      }
    }
  ]
}
```
(con su contraseña del paso 2 — este archivo nunca se sube a GitHub, ya está en el .gitignore)

---

O todo de una vez:

```bash
cat 01_database.sql 02_tables.sql 03_indexes.sql 04_views.sql 05_seed_data.sql | mysql -u root -p
```

Ver `00_ANALISIS_Y_ERD.md` para el análisis del modelo, el diagrama entidad-
relación, la explicación de tablas/relaciones/reglas de integridad, y las
estrategias transaccional y de concurrencia recomendadas para Spring Boot.

Usuarios de prueba (ver sección de seguridad en 05_seed_data.sql): username
`admin` / `vendedor1`, contraseña `password` para ambos (hash BCrypt de
ejemplo — cámbialo antes de producción).
