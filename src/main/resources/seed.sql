-- ============================================================
--  DATOS DE EJEMPLO (los mismos del padrón del informe)
-- ============================================================

INSERT INTO tipo_personeria (idtipopersoneria, nombre) VALUES
    (1, 'Natural'),
    (2, 'Jurídica');

INSERT INTO usuario (idusuario, usuario, clave, nombre, rol, estado) VALUES
    (1, 'admin', 'admin123', 'Administrador', 'ADMIN', 'Activo');

INSERT INTO departamento (iddepartamento, numero, piso, direccion, precio_mensual, estado) VALUES
    (1, '101', 1, 'Jr. San Martin 120', 650.00, 'Ocupado'),
    (2, '102', 1, 'Jr. San Martin 120', 500.00, 'Ocupado'),
    (3, '201', 2, 'Jr. San Martin 120', 700.00, 'Ocupado'),
    (4, '202', 2, 'Jr. San Martin 120', 480.00, 'Disponible'),
    (5, '301', 3, 'Jr. San Martin 120', 900.00, 'Ocupado'),
    (6, '302', 3, 'Jr. San Martin 120', 720.00, 'Disponible');

INSERT INTO inquilino (idinquilino, dni, nombre, apellido, telefono, direccion, idtipopersoneria, rep_legal, ruc, direccion_fiscal) VALUES
    (1, '27440013', 'Ana', 'Quispe', '913424322', 'Jr. San Martin 120', 1, NULL, NULL, NULL),
    (2, '45650699', 'Luis', 'Mamani', '987444566', 'Av. Circunvalacion 250', 1, NULL, NULL, NULL),
    (3, '18195497', 'Rosa', 'Condori', '972856927', 'Jr. Puno 315', 1, NULL, NULL, NULL),
    (4, '72664197', 'Pedro', 'Flores', '965210378', 'Jr. Arequipa 180', 1, NULL, NULL, NULL),
    (5, '40434926', 'Elena', 'Huanca', '982456900', 'Av. Tacna 420', 1, NULL, NULL, NULL);

INSERT INTO contrato (idcontrato, fecha_inicio, fecha_fin, monto_mensual, estado, idinquilino, iddepartamento, fecha_contrato, cantidad_persona) VALUES
    (1, '2026-03-01', '2027-02-28', 650.00, 'Vigente', 1, 1, '2026-02-25 09:00:00', 2),
    (2, '2026-04-01', '2027-03-31', 500.00, 'Vigente', 2, 2, '2026-03-26 10:30:00', 1),
    (3, '2026-05-01', '2027-04-30', 700.00, 'Vigente', 3, 3, '2026-04-26 11:15:00', 3),
    (4, '2026-06-01', '2027-05-31', 900.00, 'Vigente', 4, 5, '2026-05-27 08:45:00', 4),
    (5, '2025-01-01', '2025-12-31', 450.00, 'Finalizado', 5, 4, '2024-12-27 15:00:00', 2);

INSERT INTO pago (idpago, fecha_pago, periodo, monto, metodo, idcontrato, descuento, igv) VALUES
    (1, '2026-09-02', '2026-09-01', 650.00, 'Transferencia', 1, 0, 0),
    (2, '2026-10-03', '2026-10-01', 650.00, 'Yape', 1, 0, 0),
    (3, '2026-09-04', '2026-09-01', 500.00, 'Efectivo', 2, 0, 0),
    (4, '2026-10-05', '2026-10-01', 500.00, 'Transferencia', 2, 0, 0),
    (5, '2026-09-03', '2026-09-01', 700.00, 'Yape', 3, 0, 0),
    (6, '2026-10-06', '2026-10-01', 700.00, 'Transferencia', 3, 0, 0),
    (7, '2026-09-05', '2026-09-01', 900.00, 'Efectivo', 4, 0, 0),
    (8, '2025-12-02', '2025-12-01', 450.00, 'Efectivo', 5, 0, 0);

INSERT INTO finalizacion_contrato (id_finalizacion, idcontrato, fecha, motivo) VALUES
    (1, 5, '2025-12-31 18:00:00', 'Fin de plazo');

-- Reiniciar los contadores de las claves generadas
ALTER TABLE usuario ALTER COLUMN idusuario RESTART WITH 2;
ALTER TABLE departamento ALTER COLUMN iddepartamento RESTART WITH 7;
ALTER TABLE inquilino ALTER COLUMN idinquilino RESTART WITH 6;
ALTER TABLE contrato ALTER COLUMN idcontrato RESTART WITH 6;
ALTER TABLE pago ALTER COLUMN idpago RESTART WITH 9;
ALTER TABLE finalizacion_contrato ALTER COLUMN id_finalizacion RESTART WITH 2;
