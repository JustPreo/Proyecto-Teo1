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

