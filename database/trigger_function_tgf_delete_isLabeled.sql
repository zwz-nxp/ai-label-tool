-- FUNCTION: public.tgf_delete_isLabeled()

-- DROP FUNCTION IF EXISTS public."tgf_delete_isLabeled"();

CREATE OR REPLACE FUNCTION public."tgf_delete_isLabeled"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
declare
   cn_image_label_count integer;
begin
   select count(1) into cn_image_label_count from public.la_images_label
   where image_id=OLD.image_id
   ;
   if cn_image_label_count=0 then
      RAISE NOTICE 'there are % labels in table.', cn_image_label_count;
	  update public.la_images
	  set is_labeled=false
	  where id=OLD.image_id
	  ;
   end if;
   return OLD;
end;
$BODY$;

ALTER FUNCTION public."tgf_delete_isLabeled"()
    OWNER TO postgres;

COMMENT ON FUNCTION public."tgf_delete_isLabeled"()
    IS 'update isLabeled flag to false.';
