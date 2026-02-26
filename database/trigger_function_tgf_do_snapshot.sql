-- FUNCTION: public.tgf_do_snapshot()

-- DROP FUNCTION IF EXISTS public.tgf_do_snapshot();

CREATE OR REPLACE FUNCTION public.tgf_do_snapshot()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
declare
   v_project_id integer:=NEW.project_id;
   v_snapshot_id integer := NEW.id;
   
   cn_images_count integer;
   cn_images_tag_count integer;
   cn_images_label_count integer;
   cn_images_metadata_count integer;
   cn_project_class_count integer;
   cn_project_metadata_count integer;
   cn_project_split_count integer;
   cn_project_tag_count integer;
begin
   --do snapshot to table la_images_ss
   select count(1) into cn_images_count from public.la_images
   where project_id=v_project_id
   ;
   if cn_images_count>0 then
      RAISE NOTICE 'la_images has  % images.', cn_images_count;
	  INSERT INTO public.la_images_ss
	  (id, project_id, file_name, file_size, width, height, split, is_no_class, 
	   thumbnail_image, snapshot_id, created_at, created_by, is_labeled, 
	   thumbnail_width_ratio, thumbnail_height_ratio, file_id
	  )
	  select id, project_id, file_name, file_size, width, height, split, is_no_class, 
	         thumbnail_image, v_snapshot_id, created_at, created_by, is_labeled, 
	         thumbnail_width_ratio, thumbnail_height_ratio, file_id
	  from public.la_images
	  where project_id=v_project_id
	  ;
   end if;
   --do snapshot to table la_images_tag_ss 
   select count(1) into cn_images_tag_count from public.la_images_tag
   where image_id in (select id from la_images where project_id = v_project_id)
   ;

   if cn_images_tag_count>0 then
      RAISE NOTICE 'la_images_tag has % tags', cn_images_tag_count;
	  INSERT INTO public.la_images_tag_ss
	  (id, image_id, tag_id, snapshot_id, created_at, created_by)
	  select id, image_id, tag_id, v_snapshot_id, created_at, created_by
	  from public.la_images_tag
	  where image_id in (select id from la_images where project_id = v_project_id)
	  ;
   end if;

   --do snapshot to table la_images_label_ss 
   select count(1) into cn_images_label_count from public.la_images_label
   where image_id in (select id from la_images where project_id = v_project_id)
   ;

   if cn_images_label_count>0 then
      RAISE NOTICE 'la_images_label has % labels', cn_images_label_count;
	  INSERT INTO public.la_images_label_ss
	  (id, image_id, class_id, "position", snapshot_id, created_at, created_by)
	  select id, image_id, class_id, "position", v_snapshot_id, created_at, created_by
	  from public.la_images_label
	  where image_id in (select id from la_images where project_id = v_project_id)
	  ;
   end if;

   --do snapshot to table la_images_metadata_ss 
   select count(1) into cn_images_metadata_count from public.la_images_metadata
   where image_id in (select id from la_images where project_id = v_project_id)
   ;

   if cn_images_metadata_count>0 then
      RAISE NOTICE 'la_images_metadata has % metadatas', cn_images_metadata_count;
	  INSERT INTO public.la_images_metadata_ss
	  (id, image_id, metadata_id, value, snapshot_id, created_at, created_by)
	  select id, image_id, metadata_id, value, v_snapshot_id, created_at, created_by
	  from public.la_images_metadata
	  where image_id in (select id from la_images where project_id = v_project_id)
	  ;
   end if;

   --do snapshot to table la_project_class_ss 
   select count(1) into cn_project_class_count from public.la_project_class
   where project_id = v_project_id
   ;

   if cn_project_class_count>0 then
      RAISE NOTICE 'la_project_class_ss has % classes', cn_project_class_count;
	  -- Insert with sequence number starting from 0
	  INSERT INTO public.la_project_class_ss
	  (id, project_id, class_name, description, color_code, snapshot_id, created_at, created_by, sequence)
	  select 
	    id, 
	    project_id, 
	    class_name, 
	    description, 
	    color_code, 
	    v_snapshot_id, 
	    created_at, 
	    created_by,
	    ROW_NUMBER() OVER (ORDER BY id) - 1 AS sequence  -- Generate sequence 0, 1, 2, ...
	  from public.la_project_class
	  where project_id = v_project_id
	  ;
   end if;

   --do snapshot to table la_project_metadata_ss 
   select count(1) into cn_project_metadata_count from public.la_project_metadata
   where project_id = v_project_id
   ;

   if cn_project_metadata_count>0 then
      RAISE NOTICE 'la_project_metadata_ss has % metadatas', cn_project_metadata_count;
	  INSERT INTO public.la_project_metadata_ss
	  (id, project_id, name, type, value_from, predefined_values, multiple_values, snapshot_id, created_at, created_by)
	  select id, project_id, name, type, value_from, predefined_values, multiple_values, v_snapshot_id, created_at, created_by
	  from public.la_project_metadata
	  where project_id = v_project_id
	  ;
   end if;

   --do snapshot to table la_project_split_ss 
   select count(1) into cn_project_split_count from public.la_project_split
   where project_id = v_project_id
   ;

   if cn_project_split_count>0 then
      RAISE NOTICE 'la_project_split_ss has % splits', cn_project_split_count;
	  INSERT INTO public.la_project_split_ss
	  (id, project_id, train_ratio, dev_ratio, test_ratio, class_id, snapshot_id, created_at, created_by)
	  select id, project_id, train_ratio, dev_ratio, test_ratio, class_id, v_snapshot_id, created_at, created_by
	  from public.la_project_split
	  where project_id = v_project_id
	  ;
   end if;

   --do snapshot to table la_project_tag_ss 
   select count(1) into cn_project_tag_count from public.la_project_tag
   where project_id = v_project_id
   ;

   if cn_project_tag_count>0 then
      RAISE NOTICE 'la_project_tag_ss has % tags', cn_project_tag_count;
	  INSERT INTO public.la_project_tag_ss
	  (id, project_id, name, snapshot_id, created_at, created_by)
	  select id, project_id, name, v_snapshot_id, created_at, created_by
	  from public.la_project_tag
	  where project_id = v_project_id
	  ;
   end if;

   return NEW;
end;
$BODY$;

ALTER FUNCTION public.tgf_do_snapshot()
    OWNER TO postgres;
