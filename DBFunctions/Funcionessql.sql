--1
CREATE OR ALTER FUNCTION dbo.fn_obtener_categoria_por_subcategoria(@id_subcategoria INT)
RETURNS INT
AS
BEGIN
	DECLARE @id_cat INT
SELECT @id_cat = subc.id_categoria 
from subcategoria subc 
where @id_subcategoria = subc.id_subcategoria
return @id_cat
END;


--2
CREATE OR ALTER FUNCTION dbo.fn_calcular_monto_ejecutado(@anio int ,@mes INT, @id_subcategoria INT)
RETURNS DECIMAL(12,2)--guardar 2 decimales y 10 numeros grandes osea max 9,999,999,999supongo
AS
BEGIN
	declare @monto decimal(12,2)
	select @monto = COALESCE(sum(t.monto),0) from transaccion t 
	where t.ano = @anio and t.mes = @mes and @id_subcategoria = t.id_subcategoria
	return @monto
END;


--3
CREATE OR ALTER FUNCTION dbo.fn_validar_vigencia_presupuesto(@fecha DATE, @id_presupuesto INT)
RETURNS VARCHAR(1)
AS 
BEGIN
DECLARE @RESULTADO varchar(1) = 'N'
DECLARE @anio INT;
DECLARE @mes INT;

SET @anio = YEAR(@fecha);
SET @mes = MONTH(@fecha);

if exists (SELECT 1 from presupuesto p
where @id_presupuesto = p.id_presupuesto and 
		DATEFROMPARTS(@anio, @mes, 1)
          BETWEEN DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1)
          AND DATEFROMPARTS(p.ano_fin, p.mes_fin, 1))
          BEGIN
          SET @RESULTADO = 'S';
		  END;
	return @RESULTADO

	
END;

--4
CREATE OR ALTER FUNCTION dbo.fn_obtener_total_ejecutado_categoria_mes(@id_categoria INT, @anio int, @mes INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
	DECLARE @monto DECIMAL(12,2)
	SELECT @monto = coalesce(SUM(t.monto),0) from transaccion t 
	inner join subcategoria sub 
	on t.id_subcategoria = sub.id_subcategoria 
	and t.ano = @anio and t.mes = @mes
	and @id_categoria = sub.id_categoria
	
	return @monto
END



CREATE OR ALTER FUNCTION dbo.fn_obtener_total_categoria_mes(@id_categoria INT,
@id_presupuesto INT, @anio INT, @mes INT)
RETURNS DECIMAL(12,2)
AS 
BEGIN
	DECLARE @monto DECIMAL(12,2)
	SELECT @monto = COALESCE(SUM(pd.monto_mensual),0) FROM presupuesto p
	inner join presupuesto_detalle pd on p.id_presupuesto = pd.id_presupuesto
	inner join subcategoria sub on sub.id_subcategoria = pd.id_subcategoria
	where @id_presupuesto = pd.id_presupuesto and @id_categoria = sub.id_categoria 
	and DATEFROMPARTS(@anio, @mes, 1)
          BETWEEN DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1)
          AND DATEFROMPARTS(p.ano_fin, p.mes_fin, 1);
	
	return @monto
END


--6 fn_obtener_balance_subcategoria(id_presupuesto, id_subcategoria, anio, mes)

CREATE OR ALTER FUNCTION dbo.fn_obtener_balance_subcategoria(@id_presupuesto INT, 
@id_subcategoria INT, @anio INT , @mes INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
	DECLARE @gastado DECIMAL(12,2)
	DECLARE @presupuestoD DECIMAL(12,2)
	
	select @presupuestoD = COALESCE(SUM(pd.monto_mensual),0) 
	from presupuesto_detalle pd
	where pd.id_subcategoria = @id_subcategoria 
	and pd.id_presupuesto = @id_presupuesto
	
	
	select @gastado = COALESCE(SUM(t.monto),0) 
	from transaccion t where
	t.ano = @anio and t.mes = @mes
	and t.id_subcategoria = @id_subcategoria
	and t.id_presupuesto = @id_presupuesto
	
	return @presupuestoD - @gastado
	
	
END



--7 fn_calcular_porcentaje_ejecutado(id_subcategoria, id_presupuesto, anio, mes)
CREATE OR ALTER FUNCTION dbo.fn_calcular_porcentaje_ejecutado(@id_subcategoria INT, @id_presupuesto INT, @anio INT, @mes INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
	DECLARE @gastado DECIMAL(12,2)
	DECLARE @presupuestoD DECIMAL(12,2)
	
	select @presupuestoD = COALESCE(SUM(pd.monto_mensual),0) 
	from presupuesto_detalle pd
	where pd.id_subcategoria = @id_subcategoria 
	and pd.id_presupuesto = @id_presupuesto
	
	
	select @gastado = COALESCE(SUM(t.monto),0) 
	from transaccion t where
	t.ano = @anio and t.mes = @mes
	and t.id_subcategoria = @id_subcategoria
	and t.id_presupuesto = @id_presupuesto
	
	IF (@presupuestoD != 0)
		return (@gastado / @presupuestoD)*100
	return 0
	
END




--8 fn_dias_hasta_vencimiento

CREATE OR ALTER FUNCTION dbo.fn_dias_hasta_vencimiento(@id_obligacion INT)
RETURNS INT
AS
BEGIN
	DECLARE @hoy DATE = CAST(GETDATE() AS DATE);
    DECLARE @dia_vencimiento INT;
    DECLARE @vigente BIT;
    DECLARE @fecha_inicio DATE;
    DECLARE @fecha_fin DATE;
    DECLARE @ultimo_dia_mes INT;
    DECLARE @dia_real INT;
    DECLARE @fecha_vencimiento DATE;

	SELECT @dia_vencimiento = dia_vencimiento,@vigente = vigente,
	        @fecha_inicio = fecha_inicio,@fecha_fin = fecha_fin
	    FROM obligacion_fija
	    WHERE id_obligacion = @id_obligacion;
	
	IF @dia_vencimiento IS NULL or @vigente = 0 
	        RETURN NULL;
	
	IF @hoy < @fecha_inicio OR (@fecha_fin IS NOT NULL AND @hoy > @fecha_fin)
	        RETURN NULL;
	
	SET @ultimo_dia_mes = DAY(EOMONTH(@hoy));
	--mas que nada verificacion pa evitar un 31 de febrero y asi
	IF @dia_vencimiento > @ultimo_dia_mes
	    SET @dia_real = @ultimo_dia_mes;
	ELSE
	    SET @dia_real = @dia_vencimiento;


	SET @fecha_vencimiento =DATEFROMPARTS(YEAR(@hoy),MONTH(@hoy),@dia_real);
	
	RETURN DATEDIFF(DAY, @hoy, @fecha_vencimiento);
	
	
	
END


--9 fn_obtener_promedio_gasto_subcategoria

CREATE OR ALTER FUNCTION dbo.fn_obtener_promedio_gasto_subcategoria(
	@id_usuario INT,@id_subcategoria INT,
	@cantidad_meses INT
)
RETURNS DECIMAL(12,2)
AS BEGIN
	DECLARE @total_gastado DECIMAL(12,2);
	DECLARE @promedio DECIMAL(12,2);
	DECLARE @mes_actual DATE;
	DECLARE @mes_inicial DATE;

	if (@cantidad_meses) <= 0
		RETURN NULL;
	
	set @mes_actual = DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1);
	
	set @mes_inicial= DATEADD(MONTH, 1 - @cantidad_meses, @mes_actual);
	SELECT @total_gastado = COALESCE(SUM(T.monto),0)
	FROM transaccion T where @id_usuario = T.id_usuario
	and T.id_subcategoria = @id_subcategoria
	and T.tipo = 2
	and DATEFROMPARTS(t.ano, t.mes, 1) BETWEEN @mes_inicial AND @mes_actual;
	
	SET @promedio = @total_gastado / @cantidad_meses;

    RETURN @promedio;
END;


--10 fn_calcular_proyeccion_gasto_mensual

CREATE OR ALTER FUNCTION dbo.fn_calcular_proyeccion_gasto_mensual(
	@id_subcategoria INT,@anio INT,@mes INT
)
RETURNS DECIMAL(12,2)
AS 
BEGIN
	DECLARE @gastado DECIMAL(12,2)
	DECLARE @proyeccion DECIMAL(12,2)
	DECLARE @fecha_mes DATE;
	DECLARE @mes_actual DATE;
	DECLARE @dias_pasados INT;
	DECLARE @dias_totales INT;

	SET @fecha_mes = DATEFROMPARTS(@anio , @mes, 1);
	SET @mes_actual = DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1);
	
	SELECT @gastado = COALESCE(sum(t.monto),0) FROM
	transaccion t where
	t.id_subcategoria = @id_subcategoria
	AND t.ano = @anio
	AND t.mes = @mes
	AND t.tipo = 2
	
	if (@fecha_mes > @mes_actual)
		RETURN NULL
	if (@fecha_mes < @mes_actual)
		RETURN @gastado
	
		SET @dias_pasados = DAY(GETDATE());
		SET @dias_totales = DAY(EOMONTH(@fecha_mes));
		
		SET @proyeccion = (@gastado / @dias_pasados) * @dias_totales;
		
		return @proyeccion;
END








	
