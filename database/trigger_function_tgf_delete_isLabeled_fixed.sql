-- FUNCTION: public.tgf_delete_isLabeled()
-- This trigger sets is_labeled=false when the last label is deleted
-- BUT only if is_no_class is not true

-- DROP FUNCTION IF EXISTS public."tgf_delete_isLabeled"();

CREATE OR REPLACE FUNCTION public."tgf_delete_isLabeled"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
   cn_image_label_count INTEGER;
   is_no_class_flag BOOLEAN;
BEGIN
   -- Count remaining labels for this image
   SELECT COUNT(1) INTO cn_image_label_count 
   FROM public.la_images_label
   WHERE image_id = OLD.image_id;
   
   -- Check if the image is marked as no_class
   SELECT is_no_class INTO is_no_class_flag 
   FROM public.la_images
   WHERE id = OLD.image_id;
   
   -- Only set is_labeled=false if:
   -- 1. No labels remain
   -- 2. is_no_class is false or null
   IF cn_image_label_count = 0 THEN
      IF is_no_class_flag IS NULL OR is_no_class_flag = FALSE THEN
         RAISE NOTICE 'No labels remain and is_no_class is false, setting is_labeled=false';
         UPDATE public.la_images
         SET is_labeled = FALSE
         WHERE id = OLD.image_id;
      ELSE
         RAISE NOTICE 'No labels remain but is_no_class is true, keeping is_labeled=true';
      END IF;
   END IF;
   
   RETURN OLD;
END;
$BODY$;

ALTER FUNCTION public."tgf_delete_isLabeled"()
    OWNER TO postgres;

COMMENT ON FUNCTION public."tgf_delete_isLabeled"()
    IS 'Update isLabeled flag to false when last label is deleted, but only if is_no_class is false.';
