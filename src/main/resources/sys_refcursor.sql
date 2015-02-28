DECLARE
  v_sql VARCHAR2(32767);
  v_refcursor SYS_REFCURSOR;
  v_column_count NUMBER := 8;
BEGIN
  v_sql := 'SELECT ';
  FOR i IN 1 .. v_column_count LOOP
    v_sql := v_sql || to_char(i) || ' AS column_' || to_char(i);
    IF NOT i = v_column_count THEN
      v_sql := v_sql || ', ';
    END IF;
  END LOOP;
  v_sql := v_sql || ' FROM dual';
  dbms_output.put_line('sql = ' || v_sql);
  OPEN v_refcursor FOR v_sql;
  ? := v_refcursor;
END;