/*
    julio y agosto de 2026.

*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (SELECT 1 FROM dbo.usuario WHERE correo_electronico = 'said@gmail.com')
        THROW 51000, 'Los datos demo ya existen. No se insertaron duplicados.', 1;

    DECLARE @creado_por VARCHAR(100) = 'datos_prueba';
    DECLARE @id_usuario INT;
    DECLARE @id_presupuesto INT;
    DECLARE @id_cat_ingreso INT, @id_cat_alimentacion INT, @id_cat_transporte INT,
            @id_cat_vivienda INT, @id_cat_servicios INT, @id_cat_ahorro INT;
    DECLARE @id_salario INT, @id_extra INT, @id_supermercado INT, @id_restaurantes INT,
            @id_combustible INT, @id_transporte_publico INT, @id_alquiler INT,
            @id_electricidad INT, @id_agua INT, @id_internet INT, @id_emergencia INT;
    DECLARE @id_obl_alquiler INT, @id_obl_electricidad INT, @id_obl_internet INT;

    EXEC dbo.sp_insertar_usuario
        @nombre = 'Said',
        @apellido = 'Napky',
        @correo_electronico = 'said@gmail.com',
        @salario_base = 28000.00,
        @creado_por = @creado_por;

    SELECT @id_usuario = id_usuario
    FROM dbo.usuario
    WHERE correo_electronico = 'said@gmail.com';

    EXEC dbo.sp_insertar_categoria 'Salario Principal', 'Ingresos regulares del usuario', 1, 1, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Ingresos Extra', 'Bonos y trabajos adicionales', 1, 2, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Alimentacion', 'Compras y comidas', 2, 1, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Transporte', 'Movilidad del usuario', 2, 2, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Vivienda', 'Alquiler y mantenimiento', 2, 3, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Servicios', 'Servicios del hogar', 2, 4, @creado_por;
    EXEC dbo.sp_insertar_categoria 'Fondo de Emergencia', 'Ahorro para emergencias', 3, 1, @creado_por;

    SELECT @id_cat_ingreso = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Salario Principal' AND tipo_categoria = 1;
    SELECT @id_cat_alimentacion = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Alimentacion' AND tipo_categoria = 2;
    SELECT @id_cat_transporte = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Transporte' AND tipo_categoria = 2;
    SELECT @id_cat_vivienda = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Vivienda' AND tipo_categoria = 2;
    SELECT @id_cat_servicios = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Servicios' AND tipo_categoria = 2;
    SELECT @id_cat_ahorro = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Fondo de Emergencia' AND tipo_categoria = 3;

    SELECT @id_extra = id_categoria FROM dbo.categoria WHERE nombre_categoria = 'Ingresos Extra' AND tipo_categoria = 1;

    SELECT @id_salario = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_ingreso AND es_default = 1;
    SELECT @id_emergencia = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_ahorro AND es_default = 1;

    EXEC dbo.sp_insertar_subcategoria @id_cat_ingreso, 'Salario Base', 'Salario mensual principal', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_extra, 'Bonificaciones', 'Bonos recibidos', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_alimentacion, 'Supermercado', 'Compras del hogar', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_alimentacion, 'Restaurantes', 'Comidas fuera de casa', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_transporte, 'Combustible', 'Combustible del vehículo', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_transporte, 'Transporte Publico', 'Buses y taxis', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_vivienda, 'Alquiler', 'Alquiler mensual', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_servicios, 'Electricidad', 'Servicio eléctrico', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_servicios, 'Agua', 'Servicio de agua', @creado_por;
    EXEC dbo.sp_insertar_subcategoria @id_cat_servicios, 'Internet', 'Servicio de internet', @creado_por;

    SELECT @id_salario = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_ingreso AND nombre = 'Salario Base';
    SELECT @id_extra = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_extra AND nombre = 'Bonificaciones';
    SELECT @id_supermercado = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_alimentacion AND nombre = 'Supermercado';
    SELECT @id_restaurantes = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_alimentacion AND nombre = 'Restaurantes';
    SELECT @id_combustible = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_transporte AND nombre = 'Combustible';
    SELECT @id_transporte_publico = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_transporte AND nombre = 'Transporte Publico';
    SELECT @id_alquiler = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_vivienda AND nombre = 'Alquiler';
    SELECT @id_electricidad = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_servicios AND nombre = 'Electricidad';
    SELECT @id_agua = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_servicios AND nombre = 'Agua';
    SELECT @id_internet = id_subcategoria FROM dbo.subcategoria WHERE id_categoria = @id_cat_servicios AND nombre = 'Internet';

    EXEC dbo.sp_insertar_presupuesto
        @id_usuario, 'Presupuesto Julio-Agosto 2026', 2026, 7, 2026, 8,
        30000.00, 18000.00, 5000.00, @creado_por;

    SELECT @id_presupuesto = id_presupuesto
    FROM dbo.presupuesto
    WHERE id_usuario = @id_usuario
      AND nombre_descriptivo = 'Presupuesto Julio-Agosto 2026';

    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_salario, 28000.00, 'Salario mensual', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_extra, 2000.00, 'Bonificación estimada', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_supermercado, 3500.00, 'Compras mensuales', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_restaurantes, 1800.00, 'Comidas fuera', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_combustible, 2200.00, 'Combustible mensual', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_transporte_publico, 700.00, 'Transporte ocasional', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_alquiler, 8500.00, 'Alquiler mensual', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_electricidad, 1200.00, 'Promedio mensual', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_agua, 500.00, 'Servicio de agua', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_internet, 700.00, 'Internet mensual', @creado_por;
    EXEC dbo.sp_insertar_presupuesto_detalle @id_presupuesto, @id_emergencia, 2500.00, 'Ahorro mensual', @creado_por;

    EXEC dbo.sp_insertar_obligacion @id_usuario, @id_alquiler, 'Alquiler de vivienda', 'Pago mensual de vivienda', 8500.00, 5, '2026-07-01', NULL, @creado_por;
    EXEC dbo.sp_insertar_obligacion @id_usuario, @id_electricidad, 'Factura de electricidad', 'Pago mensual de energía', 1200.00, 20, '2026-07-01', NULL, @creado_por;
    EXEC dbo.sp_insertar_obligacion @id_usuario, @id_internet, 'Servicio de internet', 'Internet del hogar', 700.00, 15, '2026-07-01', NULL, @creado_por;

    SELECT @id_obl_alquiler = id_obligacion FROM dbo.obligacion_fija WHERE id_usuario = @id_usuario AND nombre = 'Alquiler de vivienda';
    SELECT @id_obl_electricidad = id_obligacion FROM dbo.obligacion_fija WHERE id_usuario = @id_usuario AND nombre = 'Factura de electricidad';
    SELECT @id_obl_internet = id_obligacion FROM dbo.obligacion_fija WHERE id_usuario = @id_usuario AND nombre = 'Servicio de internet';

    -- Julio: salario, bono, gastos variables, ahorro y obligaciones pagadas.
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_salario, 1, 'Salario de julio', 28000.00, '2026-07-01', 'transferencia', @creado_por, 'SAL-JUL-2026', 'Ingreso mensual', NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_extra, 1, 'Bono por desempeño', 1800.00, '2026-07-10', 'transferencia', @creado_por, 'BON-JUL-2026', 'Ingreso extra', NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_supermercado, 2, 'Compra quincenal', 1850.00, '2026-07-04', 'tarjeta_debito', @creado_por, 'SUP-JUL-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_supermercado, 2, 'Compra de despensa', 1420.00, '2026-07-18', 'tarjeta_debito', @creado_por, 'SUP-JUL-02', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_restaurantes, 2, 'Almuerzo familiar', 980.00, '2026-07-12', 'tarjeta_credito', @creado_por, 'RES-JUL-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_restaurantes, 2, 'Cena de fin de mes', 760.00, '2026-07-26', 'tarjeta_credito', @creado_por, 'RES-JUL-02', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_combustible, 2, 'Combustible primera quincena', 1100.00, '2026-07-07', 'tarjeta_debito', @creado_por, 'COM-JUL-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_combustible, 2, 'Combustible segunda quincena', 980.00, '2026-07-22', 'tarjeta_debito', @creado_por, 'COM-JUL-02', NULL, NULL;
    EXEC dbo.sp_insertar_transaccion @id_usuario, @id_presupuesto, @id_transporte_publico, NULL, 2026, 7, 2, 'Taxi y bus', 420.00, '2026-07-14', 'efectivo', NULL, NULL, @creado_por;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_alquiler, 2, 'Pago de alquiler julio', 8500.00, '2026-07-05', 'transferencia', @creado_por, 'ALQ-JUL-2026', NULL, @id_obl_alquiler;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_electricidad, 2, 'Pago electricidad julio', 1150.00, '2026-07-19', 'transferencia', @creado_por, 'ELE-JUL-2026', NULL, @id_obl_electricidad;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_internet, 2, 'Pago internet julio', 700.00, '2026-07-15', 'transferencia', @creado_por, 'INT-JUL-2026', NULL, @id_obl_internet;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 7, @id_emergencia, 3, 'Ahorro de julio', 2500.00, '2026-07-30', 'transferencia', @creado_por, 'AHO-JUL-2026', NULL, NULL;

    -- Agosto: variación en gastos y una obligación pendiente para probar el Reporte 4.
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_salario, 1, 'Salario de agosto', 28000.00, '2026-08-01', 'transferencia', @creado_por, 'SAL-AGO-2026', 'Ingreso mensual', NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_extra, 1, 'Trabajo freelance', 2300.00, '2026-08-08', 'transferencia', @creado_por, 'BON-AGO-2026', 'Ingreso extra', NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_supermercado, 2, 'Compra quincenal', 2100.00, '2026-08-03', 'tarjeta_debito', @creado_por, 'SUP-AGO-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_supermercado, 2, 'Compra de despensa', 1650.00, '2026-08-20', 'tarjeta_debito', @creado_por, 'SUP-AGO-02', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_restaurantes, 2, 'Almuerzo de trabajo', 1250.00, '2026-08-11', 'tarjeta_credito', @creado_por, 'RES-AGO-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_restaurantes, 2, 'Cena familiar', 1050.00, '2026-08-24', 'tarjeta_credito', @creado_por, 'RES-AGO-02', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_combustible, 2, 'Combustible agosto', 2350.00, '2026-08-09', 'tarjeta_debito', @creado_por, 'COM-AGO-01', NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_transporte_publico, 2, 'Transporte público agosto', 510.00, '2026-08-17', 'efectivo', @creado_por, NULL, NULL, NULL;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_alquiler, 2, 'Pago de alquiler agosto', 8500.00, '2026-08-05', 'transferencia', @creado_por, 'ALQ-AGO-2026', NULL, @id_obl_alquiler;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_electricidad, 2, 'Pago electricidad agosto', 1280.00, '2026-08-21', 'transferencia', @creado_por, 'ELE-AGO-2026', NULL, @id_obl_electricidad;
    EXEC dbo.sp_registrar_transaccion_completa @id_usuario, @id_presupuesto, 2026, 8, @id_emergencia, 3, 'Ahorro de agosto', 2500.00, '2026-08-30', 'transferencia', @creado_por, 'AHO-AGO-2026', NULL, NULL;

    COMMIT TRANSACTION;

    SELECT
        @id_usuario AS id_usuario_demo,
        @id_presupuesto AS id_presupuesto_demo,
        'Julio y agosto de 2026 creados correctamente' AS resultado;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
