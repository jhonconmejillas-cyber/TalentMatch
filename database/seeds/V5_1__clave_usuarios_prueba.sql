-- =====================================================================
-- Datos de prueba: nueva contrasena de los usuarios de prueba (ids 1-16).
-- Todos los usuarios de prueba tienen ahora la contrasena: talento
-- (hash BCrypt). No se edita V1_1 porque Flyway ya la aplico y validaria
-- su checksum.
-- =====================================================================
UPDATE usuario
SET contrasena_hash = '$2a$10$Luz7vvAl7D8FsQNYITZn..M7r7RBuof5GO4098hmTP2L6XuHNKjC2'
WHERE id BETWEEN 1 AND 16;
