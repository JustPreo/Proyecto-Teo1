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
    --tipo tendria que ser algo como if not null entonces esto y si es null entonces retornar todas
AS
BEGIN
	
	if (@tipo_categoria is not null and @tipo_categoria not in (1,2,3))
		throw 50009,'El tipo de categoria deberia ser 1,2 o 3',1;
	
	SELECT * from categoria where @tipo_categoria IS NULL or tipo_categoria = @tipo_categoria
END









