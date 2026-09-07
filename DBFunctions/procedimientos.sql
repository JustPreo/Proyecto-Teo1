--USUARIO
-------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.sp_insertar_usuario
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @creado_por VARCHAR(100)
AS
BEGIN
	 INSERT INTO usuario
    (nombre,apellido,
     correo_electronico,salario_base,
     creado_por
    )
    VALUES
    (@nombre,@apellido,
     @correo_electronico,@salario_base,
     @creado_por);

END



CREATE OR ALTER PROCEDURE dbo.sp_actualizar_usuario
    @id_usuario INT,
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @modificado_por VARCHAR(100)
AS
BEGIN
	--revisar que correo no exista  y ver si existe usuario/activo
	--ahi poner algo como not exists y asi
	UPDATE usuario
    SET nombre = @nombre,
        apellido = @apellido,
        correo_electronico = @correo_electronico,
        salario_base = @salario_base,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;

END



CREATE OR ALTER PROCEDURE dbo.sp_eliminar_usuario
    @id_usuario INT,@modificado_por VARCHAR(100)
AS
BEGIN
	--despues hacer verificacion si existe y si ya fue eliminado o no
	 UPDATE usuario
    SET estado = 0,modificado_por = @modificado_por,
    modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;
END


CREATE OR ALTER PROCEDURE dbo.sp_consultar_usuario
    @id_usuario INT
AS
BEGIN
	SELECT * FROM usuario
	WHERE @id_usuario = id_usuario
END


CREATE OR ALTER PROCEDURE dbo.sp_listar_usuarios
AS
BEGIN
	SELECT * FROM usuario
    ORDER BY nombre, apellido;
END

--CATEGORIA
-------------------------------------------------------------------------

CREATE OR ALTER PROCEDURE dbo.sp_insertar_categoria
    @nombre_categoria VARCHAR(50),@descripcion VARCHAR(255),
    @tipo_categoria SMALLINT,@order_presentacion SMALLINT,
    @creado_por VARCHAR(100)
AS
BEGIN

	if(@order_presentacion < 0)
		THROW 50001, 'El orden de presentacion no puede ser negativo',1;
	
	if exists(select 1 from categoria where nombre_categoria = @nombre_categoria and tipo_categoria = @tipo_categoria)
		throw 50002,'Ya existe una categoria con ese nombre y tipo',1;
	
	insert into categoria (nombre_categoria,
	descripcion,
	tipo_categoria,
	order_presentacion,
	creado_por)VALUES(
	@nombre_categoria,
	@descripcion,
	@tipo_categoria,
	@order_presentacion,
	@creado_por
	);

END;



CREATE OR ALTER PROCEDURE dbo.sp_actualizar_categoria
    @id_categoria INT,@nombre_categoria VARCHAR(50),
    @descripcion VARCHAR(255),@modificado_por VARCHAR(100)
AS
BEGIN
	if not exists(select 1 from categoria where @id_categoria = id_categoria)
		throw 50003,'La categoria no existe',1;
	if exists(select 1 from categoria where nombre_categoria = @nombre_categoria and id_categoria != @id_categoria)
		throw 50004,'Ya existe una categoria con ese nombre',1;

	UPDATE categoria set 
		nombre_categoria = @nombre_categoria,
		descripcion = @descripcion,
		modificado_por = @modificado_por,
		modificado_en = SYSDATETIME()
	WHERE id_categoria = @id_categoria
	
	

END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_categoria
    @id_categoria INT,@modificado_por VARCHAR(100)
AS
BEGIN
	if not exists(select 1 from categoria where id_categoria = @id_categoria)
		throw 50005,'La categoria no existe',1;

	if exists(select 1 from subcategoria where id_categoria = @id_categoria and es_default = 0 and estado = 1)
		throw 50006, 'La categoria tiene subcategorias activas',1;
	
	
	--subcategorias que no estan en uso 
	IF EXISTS (SELECT 1 FROM subcategoria sub WHERE sub.id_categoria = @id_categoria AND (
              EXISTS (SELECT 1 FROM presupuesto_detalle pd WHERE pd.id_subcategoria = sub.id_subcategoria)
              OR EXISTS (SELECT 1 FROM transaccion t WHERE t.id_subcategoria = sub.id_subcategoria)
              OR EXISTS (SELECT 1 FROM obligacion_fija o WHERE o.id_subcategoria = sub.id_subcategoria)
          )
    )
    	throw 50007, 'La categoria tiene subcategorias en uso',1;
	
	delete from subcategoria where id_categoria = @id_categoria;
	delete from categoria where id_categoria = @id_categoria;


END;



CREATE OR ALTER PROCEDURE dbo.sp_consultar_categoria
    @id_categoria INT
AS
BEGIN
	if not exists(select 1 from categoria where id_categoria = @id_categoria)
		throw 50008,'La categoria no existe',1;
	select * from categoria where id_categoria = @id_categoria;
	
END;


CREATE OR ALTER PROCEDURE dbo.sp_listar_categorias
    @tipo_categoria SMALLINT = NULL
AS
BEGIN
	
	if (@tipo_categoria is not null and @tipo_categoria not in (1,2,3))
		throw 50009,'El tipo de categoria deberia ser 1,2 o 3',1;
	
	SELECT * from categoria where @tipo_categoria IS NULL or tipo_categoria = @tipo_categoria
END


--SUBCATEGORIA
-------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.sp_insertar_subcategoria
    @id_categoria INT,@nombre VARCHAR(50),
    @descripcion VARCHAR(255),@creado_por VARCHAR(100)
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = @id_categoria)
        THROW 50010, 'La categoria seleccionada no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria WHERE id_categoria = @id_categoria AND nombre = @nombre)
        THROW 50011, 'Ya existe una subcategoria con ese nombre', 1;

    INSERT INTO subcategoria (id_categoria,nombre,descripcion,estado,es_default,creado_por)
    VALUES (@id_categoria,@nombre,@descripcion,1,0,@creado_por);
END;




CREATE OR ALTER PROCEDURE dbo.sp_actualizar_subcategoria
    @id_subcategoria INT,
    @nombre VARCHAR(50),
    @descripcion VARCHAR(255),
    @estado BIT,
    @modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria)
        THROW 50012, 'La subcategoria no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria sub1 INNER JOIN subcategoria sub2 ON sub1.id_categoria = sub2.id_categoria
        WHERE sub1.id_subcategoria = @id_subcategoria AND sub2.nombre = @nombre AND sub2.id_subcategoria != @id_subcategoria)
        THROW 50013, 'Ya existe una subcategoria con ese nombre', 1;

    UPDATE subcategoria SET 
    	nombre = @nombre,
        descripcion = @descripcion,
        estado = @estado,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_subcategoria
    @id_subcategoria INT
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria)
        THROW 50014, 'La subcategoria no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND es_default = 1)
        THROW 50015, 'No se puede eliminar la subcategoria default', 1;

    IF EXISTS (SELECT 1 FROM presupuesto_detalle WHERE @id_subcategoria = id_subcategoria)
        THROW 50016, 'La subcategoria esta siendo usada en un "presupuesto"', 1;

    IF EXISTS (SELECT 1 FROM transaccion WHERE id_subcategoria = @id_subcategoria)
        THROW 50017, 'La subcategoria esta siendo usada en "transacciones"', 1;

    IF EXISTS (SELECT 1 FROM obligacion_fija WHERE id_subcategoria = @id_subcategoria)
        THROW 50018, 'La subcategoria esta siendo usada en "obligaciones"', 1;

    DELETE FROM subcategoria WHERE id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_consultar_subcategoria @id_subcategoria INT
AS
BEGIN
    SELECT
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        sub.descripcion,
        sub.estado,
        sub.es_default,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        sub.creado_por,
        sub.modificado_por,
        sub.creado_en,
        sub.modificado_en
    FROM subcategoria sub
    INNER JOIN categoria c
        ON sub.id_categoria = c.id_categoria
    WHERE sub.id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_listar_subcategorias_por_categoria
    @id_categoria INT
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = @id_categoria)
        THROW 50019, 'La caegoria no existe', 1;

    SELECT * FROM subcategoria sub WHERE sub.id_categoria = @id_categoria
END;




--PRESUPUESTO
-------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.sp_insertar_presupuesto
    @id_usuario INT,@nombre VARCHAR(50),
    @ano_inicio INT,@mes_inicio SMALLINT,
    @ano_fin INT,@mes_fin SMALLINT,
    @total_ingresos DECIMAL(12,2),@total_gastos DECIMAL(12,2),
    @total_ahorro DECIMAL(12,2),@creado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 1)
        THROW 50020, 'El usuario no existe', 1;

    IF (DATEFROMPARTS(@ano_fin, @mes_fin, 1)< DATEFROMPARTS(@ano_inicio, @mes_inicio, 1))
        THROW 50021, 'La fecha final no puede ser anterior a la inicial', 1;

    IF (@total_ingresos < 0 OR @total_gastos < 0 OR @total_ahorro < 0)
        THROW 50022, 'Los montos no pueden ser negativos', 1;

    INSERT INTO presupuesto (
        id_usuario,
        nombre_descriptivo,
        ano_inicio,
        mes_inicio,
        ano_fin,
        mes_fin,
        total_ingresos,
        total_gastos,
        total_ahorro,
        estado_presupuesto,
        creado_por
    )
    VALUES (
        @id_usuario,
        @nombre,
        @ano_inicio,
        @mes_inicio,
        @ano_fin,
        @mes_fin,
        @total_ingresos,
        @total_gastos,
        @total_ahorro,
        1,
        @creado_por
    );
END;




CREATE OR ALTER PROCEDURE dbo.sp_actualizar_presupuesto
    @id_presupuesto INT,@nombre VARCHAR(50),
    @ano_inicio INT,@mes_inicio SMALLINT,
    @ano_fin INT,@mes_fin SMALLINT,
    @total_ingresos DECIMAL(12,2),@total_gastos DECIMAL(12,2),
    @total_ahorro DECIMAL(12,2),@estado_presupuesto SMALLINT,
    @modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50023, 'El presupuesto no existe', 1;

    IF (DATEFROMPARTS(@ano_fin, @mes_fin, 1)< DATEFROMPARTS(@ano_inicio, @mes_inicio, 1))
        THROW 50024, 'La fecha final no puede ser anterior a la inicial', 1;

    IF @estado_presupuesto NOT IN (1,2,3)
        THROW 50025, 'El estado debe ser 1 2 o 3', 1;

    IF @total_ingresos < 0 OR @total_gastos < 0 OR @total_ahorro < 0
        THROW 50026, 'Los montos no pueden ser negativos', 1;

    UPDATE presupuesto
    SET nombre_descriptivo = @nombre,
        ano_inicio = @ano_inicio,
        mes_inicio = @mes_inicio,
        ano_fin = @ano_fin,
        mes_fin = @mes_fin,
        total_ingresos = @total_ingresos,
        total_gastos = @total_gastos,
        total_ahorro = @total_ahorro,
        estado_presupuesto = @estado_presupuesto,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_presupuesto = @id_presupuesto;
END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_presupuesto
    @id_presupuesto INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50027, 'No existe esepresupuesto', 1;

    IF EXISTS (SELECT 1 FROM transaccion WHERE id_presupuesto = @id_presupuesto)
        THROW 50028, 'El presupuesto esta siendo utilizado en "Transaccion"', 1;

        DELETE FROM presupuesto_detalle
        WHERE id_presupuesto = @id_presupuesto;

        DELETE FROM presupuesto
        WHERE id_presupuesto = @id_presupuesto;
END;


CREATE OR ALTER PROCEDURE dbo.sp_consultar_presupuesto
    @id_presupuesto INT
AS
BEGIN
    SELECT
        p.id_presupuesto,
        p.id_usuario,
        u.nombre,
        u.apellido,
        p.nombre_descriptivo,
        p.ano_inicio,
        p.mes_inicio,
        DATEFROMPARTS(p.ano_inicio,p.mes_inicio,1) AS fecha_inicio,
        p.ano_fin,
        p.mes_fin,
        EOMONTH(DATEFROMPARTS(p.ano_fin,p.mes_fin,1)) AS fecha_fin,
        p.total_ingresos,
        p.total_gastos,
        p.total_ahorro,
        p.estado_presupuesto,
        (CASE p.estado_presupuesto
            WHEN 1 THEN 'Activo'
            WHEN 2 THEN 'Cerrado'
            WHEN 3 THEN 'Borrador'
        END)AS nombre_estado,
        p.fecha_hora_creacion,
        p.creado_por,
        p.modificado_por,
        p.creado_en,
        p.modificado_en
    FROM presupuesto p
    INNER JOIN usuario u
        ON p.id_usuario = u.id_usuario
    WHERE p.id_presupuesto = @id_presupuesto;
END;




CREATE OR ALTER PROCEDURE dbo.sp_listar_presupuestos_usuario
    @id_usuario INT,
    @estado_presupuesto SMALLINT = NULL
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50029, 'El usuario no existe', 1;

    IF @estado_presupuesto IS NOT NULL AND @estado_presupuesto NOT IN (1,2,3)
        THROW 50030, 'El estado debe ser 1 2 o 3', 1;

    SELECT
        p.id_presupuesto,
        p.nombre_descriptivo,
        DATEFROMPARTS(p.ano_inicio,p.mes_inicio,1) AS fecha_inicio,
        EOMONTH(DATEFROMPARTS(p.ano_fin,p.mes_fin,1)) AS fecha_fin,
        p.total_ingresos,
        p.total_gastos,
        p.total_ahorro,
        p.estado_presupuesto,
        (CASE p.estado_presupuesto
            WHEN 1 THEN 'Activo'
            WHEN 2 THEN 'Cerrado'
            WHEN 3 THEN 'Borrador'
        END) AS nombre_estado
    FROM presupuesto p
    WHERE p.id_usuario = @id_usuario
      AND (@estado_presupuesto IS NULL OR p.estado_presupuesto = @estado_presupuesto)
      
      --maybe despues agregar algun order by)?
END;



--PRESUPUESTO_DETALLE
-------------------------------------------------------------------------

--OBLIGACION_FIJA
-------------------------------------------------------------------------

--TRANSACCION
-------------------------------------------------------------------------




