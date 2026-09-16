CREATE OR ALTER TRIGGER tr_crear_subcategoria_default
ON categoria
AFTER INSERT
AS
BEGIN

    INSERT INTO subcategoria (
        id_categoria,
        nombre,
        descripcion,
        estado,
        es_default,
        creado_por
    )
    SELECT
        id_categoria,
        'General',
        'Subcategoria por defecto',
        1,
        1,
        creado_por
    FROM categoria
    WHERE id_categoria = (
        SELECT MAX(id_categoria)
        FROM categoria
    );
    --en teoria deberia funcionar porque agarra la ultima categoria

END;