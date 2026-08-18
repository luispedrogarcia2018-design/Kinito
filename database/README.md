# Cómo ejecutar

Orden de ejecución (cada archivo depende del anterior):

```bash
mysql -u root -p < 01_database.sql
mysql -u root -p < 02_tables.sql
mysql -u root -p < 03_indexes.sql
mysql -u root -p < 04_views.sql
mysql -u root -p < 05_seed_data.sql
mysql -u root -p < 06_test_queries.sql   # opcional: consultas de verificación/reportes
```

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
