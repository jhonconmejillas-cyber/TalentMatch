-- =====================================================================
-- TalentMatch - datos de prueba (solo desarrollo y pruebas)
--
-- Flyway lo aplica despues de V1 cuando database/seeds esta en
-- spring.flyway.locations. En produccion se quita esa carpeta con la
-- variable de entorno FLYWAY_LOCATIONS.
--
-- Todos los usuarios tienen la contrasena: TalentMatch2026
-- Las fechas son relativas a CURRENT_DATE para que las reglas de
-- antiguedad (RN-07) den el mismo resultado sin importar cuando se cargue.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Catalogo de habilidades
-- ---------------------------------------------------------------------
INSERT INTO habilidad (id, nombre) VALUES
    (1, 'Java'), (2, 'Spring Boot'), (3, 'SQL'), (4, 'PostgreSQL'), (5, 'Docker'),
    (6, 'Kubernetes'), (7, 'Go'), (8, 'Python'), (9, 'AWS'), (10, 'Terraform'),
    (11, 'React'), (12, 'JavaScript'), (13, 'TypeScript'), (14, 'Git'), (15, 'Linux'),
    (16, 'Kafka'), (17, 'Spark'), (18, 'Power BI'), (19, 'CI/CD'), (20, 'Scrum');

-- ---------------------------------------------------------------------
-- Proyectos (en areas distintas: se puede rotar entre ellas, RN-11)
-- ---------------------------------------------------------------------
INSERT INTO proyecto (id, nombre, area, estado) VALUES
    (1, 'Pasarela de Pagos', 'Backend', 'ACTIVO'),
    (2, 'Plataforma Cloud', 'Cloud', 'ACTIVO'),
    (3, 'Analitica de Clientes', 'Datos', 'ACTIVO'),
    (4, 'App Movil', 'Frontend', 'ACTIVO');

-- ---------------------------------------------------------------------
-- Usuarios. Hash BCrypt de "TalentMatch2026".
-- ---------------------------------------------------------------------
INSERT INTO usuario (id, rol, nombre, correo, contrasena_hash) VALUES
    (1,  'ADMIN',           'Administrador TalentMatch', 'admin@talentmatch.com',   '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (2,  'RRHH',            'Laura Rojas',               'rrhh@talentmatch.com',    '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (3,  'LIDER_EJECUTIVO', 'Martin Salazar',            'lider@talentmatch.com',   '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (4,  'MANAGER',         'Carlos Perez',              'carlos.perez@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (5,  'MANAGER',         'Diana Torres',              'diana.torres@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (6,  'MANAGER',         'Andres Gomez',              'andres.gomez@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (7,  'EMPLEADO',        'Juan Ramirez',              'juan.ramirez@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (8,  'EMPLEADO',        'Sofia Castro',              'sofia.castro@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (9,  'EMPLEADO',        'Mateo Vargas',              'mateo.vargas@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (10, 'EMPLEADO',        'Valentina Ruiz',            'valentina.ruiz@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (11, 'EMPLEADO',        'Santiago Mora',             'santiago.mora@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (12, 'EMPLEADO',        'Isabella Herrera',          'isabella.herrera@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (13, 'EMPLEADO',        'Nicolas Rios',              'nicolas.rios@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (14, 'EMPLEADO',        'Camila Ortiz',              'camila.ortiz@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (15, 'EMPLEADO',        'Samuel Medina',             'samuel.medina@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq'),
    (16, 'EMPLEADO',        'Mariana Diaz',              'mariana.diaz@talentmatch.com', '$2a$10$OhJMzM1I4tGIPFEjWvo.HuK8nutagxqFi2eYBsWli2g9poqPRJFKq');

-- Managers (4-6) y empleados (7-16). Juan (7) y Valentina (10) llevan menos de 6 meses en su rol.
INSERT INTO empleado (id, senioridad, anios_experiencia, area, fecha_inicio_rol, fecha_ingreso_empresa, manager_id) VALUES
    (4,  'LIDER',       12, 'Backend',  CURRENT_DATE - INTERVAL '36 months', CURRENT_DATE - INTERVAL '96 months', NULL),
    (5,  'LIDER',       10, 'Cloud',    CURRENT_DATE - INTERVAL '24 months', CURRENT_DATE - INTERVAL '72 months', NULL),
    (6,  'LIDER',        9, 'Datos',    CURRENT_DATE - INTERVAL '30 months', CURRENT_DATE - INTERVAL '60 months', NULL),
    (7,  'SENIOR',       8, 'Backend',  CURRENT_DATE - INTERVAL '4 months',  CURRENT_DATE - INTERVAL '96 months', 4),
    (8,  'SENIOR',       6, 'Backend',  CURRENT_DATE - INTERVAL '14 months', CURRENT_DATE - INTERVAL '50 months', 4),
    (9,  'JUNIOR',       1, 'Backend',  CURRENT_DATE - INTERVAL '10 months', CURRENT_DATE - INTERVAL '12 months', 4),
    (10, 'SEMI_SENIOR',  4, 'Cloud',    CURRENT_DATE - INTERVAL '3 months',  CURRENT_DATE - INTERVAL '40 months', 5),
    (11, 'SENIOR',       7, 'Cloud',    CURRENT_DATE - INTERVAL '20 months', CURRENT_DATE - INTERVAL '84 months', 5),
    (12, 'SEMI_SENIOR',  3, 'Cloud',    CURRENT_DATE - INTERVAL '8 months',  CURRENT_DATE - INTERVAL '30 months', 5),
    (13, 'SENIOR',       9, 'Datos',    CURRENT_DATE - INTERVAL '18 months', CURRENT_DATE - INTERVAL '108 months', 6),
    (14, 'SEMI_SENIOR',  5, 'Datos',    CURRENT_DATE - INTERVAL '12 months', CURRENT_DATE - INTERVAL '48 months', 6),
    (15, 'JUNIOR',       2, 'Frontend', CURRENT_DATE - INTERVAL '7 months',  CURRENT_DATE - INTERVAL '24 months', 4),
    (16, 'SEMI_SENIOR',  4, 'Frontend', CURRENT_DATE - INTERVAL '9 months',  CURRENT_DATE - INTERVAL '36 months', 4);

-- Asignaciones vigentes
INSERT INTO asignacion_proyecto (empleado_id, proyecto_id, cargo, fecha_inicio) VALUES
    (4,  1, 'Lider Tecnico Backend',  CURRENT_DATE - INTERVAL '36 months'),
    (5,  2, 'Lider Tecnico Cloud',    CURRENT_DATE - INTERVAL '24 months'),
    (6,  3, 'Lider Tecnico Datos',    CURRENT_DATE - INTERVAL '30 months'),
    (7,  1, 'Backend Developer',      CURRENT_DATE - INTERVAL '4 months'),
    (8,  1, 'Backend Developer',      CURRENT_DATE - INTERVAL '14 months'),
    (9,  1, 'Backend Developer',      CURRENT_DATE - INTERVAL '10 months'),
    (10, 2, 'Cloud Engineer',         CURRENT_DATE - INTERVAL '3 months'),
    (11, 2, 'Cloud Engineer',         CURRENT_DATE - INTERVAL '20 months'),
    (12, 2, 'DevOps Engineer',        CURRENT_DATE - INTERVAL '8 months'),
    (13, 3, 'Data Engineer',          CURRENT_DATE - INTERVAL '18 months'),
    (14, 3, 'Data Analyst',           CURRENT_DATE - INTERVAL '12 months'),
    (15, 4, 'Frontend Developer',     CURRENT_DATE - INTERVAL '7 months'),
    (16, 4, 'Frontend Developer',     CURRENT_DATE - INTERVAL '9 months');

-- Rotacion anterior de Sofia (8) para el reporte ejecutivo (RN-21)
INSERT INTO asignacion_proyecto (empleado_id, proyecto_id, cargo, fecha_inicio, fecha_fin, motivo_fin) VALUES
    (8, 3, 'Data Engineer', CURRENT_DATE - INTERVAL '50 months', CURRENT_DATE - INTERVAL '14 months', 'ROTACION_ACEPTADA');

-- Habilidades: mezcla de AVALADO (por su manager actual) y AGREGADO (pendientes de aval)
INSERT INTO habilidad_empleado (empleado_id, habilidad_id, estado, nivel_declarado, fecha_agregado, fecha_aval, avalado_por) VALUES
    -- Juan (7): Java, SQL, Docker avaladas; Kubernetes pendiente
    (7, 1,  'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '55 days', 4),
    (7, 3,  'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '55 days', 4),
    (7, 5,  'AVALADO',  'Intermedio', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '55 days', 4),
    (7, 6,  'AGREGADO', 'Experto',    CURRENT_DATE - INTERVAL '5 days',  NULL, NULL),
    -- Sofia (8)
    (8, 1,  'AVALADO',  'Experto',    CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '80 days', 4),
    (8, 2,  'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '80 days', 4),
    (8, 4,  'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '80 days', 4),
    (8, 5,  'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '80 days', 4),
    (8, 16, 'AGREGADO', 'Intermedio', CURRENT_DATE - INTERVAL '3 days',  NULL, NULL),
    -- Mateo (9)
    (9, 1,  'AVALADO',  'Intermedio', CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE - INTERVAL '35 days', 4),
    (9, 14, 'AVALADO',  'Intermedio', CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE - INTERVAL '35 days', 4),
    (9, 2,  'AGREGADO', 'Basico',     CURRENT_DATE - INTERVAL '2 days',  NULL, NULL),
    -- Valentina (10)
    (10, 5, 'AVALADO',  'Avanzado',   CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '25 days', 5),
    (10, 9, 'AVALADO',  'Intermedio', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '25 days', 5),
    -- Santiago (11)
    (11, 5,  'AVALADO', 'Experto',    CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '95 days', 5),
    (11, 6,  'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '95 days', 5),
    (11, 10, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '95 days', 5),
    (11, 9,  'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '95 days', 5),
    (11, 7,  'AGREGADO', 'Intermedio', CURRENT_DATE - INTERVAL '1 days',  NULL, NULL),
    -- Isabella (12)
    (12, 19, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '45 days', 5),
    (12, 15, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '45 days', 5),
    (12, 5,  'AVALADO', 'Intermedio', CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '45 days', 5),
    -- Nicolas (13)
    (13, 8,  'AVALADO', 'Experto',    CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '110 days', 6),
    (13, 3,  'AVALADO', 'Experto',    CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '110 days', 6),
    (13, 17, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '110 days', 6),
    (13, 16, 'AVALADO', 'Intermedio', CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '110 days', 6),
    -- Camila (14)
    (14, 3,  'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '70 days', CURRENT_DATE - INTERVAL '65 days', 6),
    (14, 18, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '70 days', CURRENT_DATE - INTERVAL '65 days', 6),
    (14, 8,  'AGREGADO', 'Intermedio', CURRENT_DATE - INTERVAL '4 days', NULL, NULL),
    -- Samuel (15)
    (15, 12, 'AVALADO', 'Intermedio', CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '15 days', 4),
    (15, 11, 'AGREGADO', 'Basico',    CURRENT_DATE - INTERVAL '6 days',  NULL, NULL),
    -- Mariana (16)
    (16, 11, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '80 days', CURRENT_DATE - INTERVAL '75 days', 4),
    (16, 12, 'AVALADO', 'Avanzado',   CURRENT_DATE - INTERVAL '80 days', CURRENT_DATE - INTERVAL '75 days', 4),
    (16, 13, 'AVALADO', 'Intermedio', CURRENT_DATE - INTERVAL '80 days', CURRENT_DATE - INTERVAL '75 days', 4);

INSERT INTO wishlist (empleado_id, habilidad_id) VALUES (7, 7), (7, 6), (9, 5), (14, 17);

-- ---------------------------------------------------------------------
-- Vacantes activas publicadas por los managers (RF-11)
-- ---------------------------------------------------------------------
INSERT INTO vacante (id, titulo, descripcion, senioridad, area, estado, origen, fecha_creacion, fecha_activacion, manager_id, proyecto_id) VALUES
    (1, 'Backend Senior', 'Desarrollo de microservicios de pagos en Java.', 'SENIOR', 'Backend', 'ACTIVA', 'MANUAL',
        CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 4, 1),
    (2, 'Cloud Engineer', 'Operacion de la plataforma sobre Kubernetes.', 'SEMI_SENIOR', 'Cloud', 'ACTIVA', 'MANUAL',
        CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 5, 2),
    (3, 'Data Engineer', 'Pipelines de datos de clientes.', 'SENIOR', 'Datos', 'ACTIVA', 'MANUAL',
        CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 6, 3),
    (4, 'Frontend Developer', 'Nueva version de la app movil.', 'SEMI_SENIOR', 'Frontend', 'ACTIVA', 'MANUAL',
        CURRENT_TIMESTAMP - INTERVAL '1 days', CURRENT_TIMESTAMP - INTERVAL '1 days', 4, 4);

INSERT INTO vacante_habilidad_critica (vacante_id, habilidad_id) VALUES
    (1, 1), (1, 2), (1, 4),
    (2, 6), (2, 5), (2, 10),
    (3, 8), (3, 3), (3, 17),
    (4, 11), (4, 13), (4, 12);

INSERT INTO vacante_habilidad_deseable (vacante_id, habilidad_id) VALUES
    (1, 5), (1, 16),
    (2, 9), (2, 7),
    (3, 16);

-- Las filas anteriores tienen id fijo: se mueven los contadores para que
-- los siguientes INSERT de la aplicacion no choquen con ellos.
SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(id) FROM usuario));
SELECT setval(pg_get_serial_sequence('habilidad', 'id'), (SELECT MAX(id) FROM habilidad));
SELECT setval(pg_get_serial_sequence('proyecto', 'id'), (SELECT MAX(id) FROM proyecto));
SELECT setval(pg_get_serial_sequence('vacante', 'id'), (SELECT MAX(id) FROM vacante));
