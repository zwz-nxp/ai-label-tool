-- FUNCTION: public.tgf_sync_no_class()
-- This trigger manages is_labeled when is_no_class changes

-- DROP FUNCTION IF EXISTS public.tgf_sync_no_class();

CREATE OR REPLACE FUNCTION public.tgf_sync_no_class()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
    label_count INTEGER;
BEGIN
    -- When is_no_class changes, we need to update is_labeled accordingly
    
    IF NEW.is_no_class = TRUE THEN
        -- Setting to no_class: is_labeled should be true
        NEW.is_labeled = TRUE;
        
        -- Delete all labels for this image (will be done by application, but ensure consistency)
        -- The application should handle label deletion before calling this
        
    ELSIF NEW.is_no_class = FALSE OR NEW.is_no_class IS NULL THEN
        -- Unsetting no_class: check if labels exist
        SELECT COUNT(1) INTO label_count 
        FROM public.la_images_label 
        WHERE image_id = NEW.id;
        
        IF label_count > 0 THEN
            NEW.is_labeled = TRUE;
        ELSE
            NEW.is_labeled = FALSE;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.tgf_sync_no_class()
    OWNER TO postgres;

COMMENT ON FUNCTION public.tgf_sync_no_class()
    IS 'Sync is_labeled with is_no_class state. When is_no_class=true, is_labeled=true. When is_no_class=false, is_labeled depends on label existence.';
