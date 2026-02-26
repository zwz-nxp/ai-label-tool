-- FUNCTION: public.tgf_update_isLabeled()
-- This trigger sets is_labeled=true when a label is inserted
-- BUT only if is_no_class is not true

-- DROP FUNCTION IF EXISTS public."tgf_update_isLabeled"();

CREATE OR REPLACE FUNCTION public."tgf_update_isLabeled"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
   is_no_class_flag BOOLEAN;
BEGIN
   -- Check if the image is marked as no_class
   SELECT is_no_class INTO is_no_class_flag 
   FROM public.la_images
   WHERE id = NEW.image_id;
   
   -- Only set is_labeled=true if is_no_class is false or null
   IF is_no_class_flag IS NULL OR is_no_class_flag = FALSE THEN
      UPDATE public.la_images
      SET is_labeled = TRUE
      WHERE id = NEW.image_id;
   END IF;
   
   RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public."tgf_update_isLabeled"()
    OWNER TO postgres;

COMMENT ON FUNCTION public."tgf_update_isLabeled"()
    IS 'Update isLabeled flag after insert in la_images_label table. Only sets to true if is_no_class is false.';
