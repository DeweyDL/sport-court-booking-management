DECLARE
PROCEDURE seed_function(p_id VARCHAR2, p_name VARCHAR2) IS
BEGIN
MERGE INTO FUNCTIONS f
    USING (SELECT p_id FUNCTION_ID, p_name NAME_FUNCTION FROM dual) s
    ON (f.FUNCTION_ID = s.FUNCTION_ID)
    WHEN MATCHED THEN UPDATE SET f.NAME_FUNCTION = s.NAME_FUNCTION, f.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (FUNCTION_ID, NAME_FUNCTION, CREATED_AT, IS_DELETED)
            VALUES (s.FUNCTION_ID, s.NAME_FUNCTION, SYSDATE, 0);
END;

    PROCEDURE seed_group(p_id VARCHAR2, p_name VARCHAR2, p_desc VARCHAR2) IS
BEGIN
MERGE INTO ROLE_GROUP g
    USING (SELECT p_id GROUP_ID, p_name GROUP_NAME, p_desc DESCRIPTION FROM dual) s
    ON (g.GROUP_ID = s.GROUP_ID)
    WHEN MATCHED THEN UPDATE SET g.GROUP_NAME = s.GROUP_NAME, g.DESCRIPTION = s.DESCRIPTION, g.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (GROUP_ID, GROUP_NAME, DESCRIPTION, CREATED_AT, IS_DELETED)
            VALUES (s.GROUP_ID, s.GROUP_NAME, s.DESCRIPTION, SYSDATE, 0);
END;

    PROCEDURE seed_role(
        p_id VARCHAR2, p_function_id VARCHAR2, p_name VARCHAR2,
        p_add NUMBER, p_edit NUMBER, p_delete NUMBER, p_download NUMBER, p_view NUMBER
    ) IS
BEGIN
MERGE INTO ROLE r
    USING (
        SELECT p_id ROLE_ID, p_function_id FUNCTION_ID, p_name ROLE_NAME,
               p_add CAN_ADD, p_edit CAN_EDIT, p_delete CAN_DELETE,
               p_download CAN_DOWNLOAD, p_view CAN_VIEW
        FROM dual
    ) s
    ON (r.ROLE_ID = s.ROLE_ID)
    WHEN MATCHED THEN UPDATE SET
        r.FUNCTION_ID = s.FUNCTION_ID,
        r.ROLE_NAME = s.ROLE_NAME,
        r.CAN_ADD = s.CAN_ADD,
        r.CAN_EDIT = s.CAN_EDIT,
        r.CAN_DELETE = s.CAN_DELETE,
        r.CAN_DOWNLOAD = s.CAN_DOWNLOAD,
        r.CAN_VIEW = s.CAN_VIEW,
        r.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (ROLE_ID, FUNCTION_ID, ROLE_NAME, CAN_ADD, CAN_EDIT, CAN_DELETE, CAN_DOWNLOAD, CAN_VIEW, CREATED_AT, IS_DELETED)
            VALUES (s.ROLE_ID, s.FUNCTION_ID, s.ROLE_NAME, s.CAN_ADD, s.CAN_EDIT, s.CAN_DELETE, s.CAN_DOWNLOAD, s.CAN_VIEW, SYSDATE, 0);
END;

    PROCEDURE seed_mapping(p_id VARCHAR2, p_group_id VARCHAR2, p_role_id VARCHAR2) IS
BEGIN
MERGE INTO ACCOUNT_ROLE_GROUP_MAPPING m
    USING (SELECT p_id MAPPING_ID, p_group_id GROUP_ID, p_role_id ROLE_ID FROM dual) s
    ON (m.MAPPING_ID = s.MAPPING_ID)
    WHEN MATCHED THEN UPDATE SET m.GROUP_ID = s.GROUP_ID, m.ROLE_ID = s.ROLE_ID, m.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (MAPPING_ID, ROLE_ID, GROUP_ID, CREATED_AT, IS_DELETED)
            VALUES (s.MAPPING_ID, s.ROLE_ID, s.GROUP_ID, SYSDATE, 0);
END;
BEGIN
    seed_function('REPORT_MANAGEMENT', 'Revenue report');
    seed_function('BRANCH_MANAGEMENT', 'Branch management');
    seed_function('CUSTOMER_MANAGEMENT', 'Customer management');
    seed_function('EQUIPMENT_MANAGEMENT', 'Equipment management');
    seed_function('PRODUCT_MANAGEMENT', 'Product management');
    seed_function('EMPLOYEE_MANAGEMENT', 'Employee management');

    seed_group('OWNER', 'Owner', 'Full system owner');
    seed_group('BRANCH_MANAGER', 'Branch manager', 'Manage own branch');
    seed_group('CASHIER', 'Cashier', 'Counter employee');
    seed_group('CUSTOMER', 'Customer', 'Registered customer');

    seed_role('R_OWNER_REPORT', 'REPORT_MANAGEMENT', 'Owner report full', 1, 1, 1, 1, 1);
    seed_role('R_OWNER_BRANCH', 'BRANCH_MANAGEMENT', 'Owner branch full', 1, 1, 1, 1, 1);
    seed_role('R_OWNER_CUSTOMER', 'CUSTOMER_MANAGEMENT', 'Owner customer full', 1, 1, 1, 1, 1);
    seed_role('R_OWNER_EQUIP', 'EQUIPMENT_MANAGEMENT', 'Owner equipment full', 1, 1, 1, 1, 1);
    seed_role('R_OWNER_PRODUCT', 'PRODUCT_MANAGEMENT', 'Owner product full', 1, 1, 1, 1, 1);
    seed_role('R_OWNER_EMP', 'EMPLOYEE_MANAGEMENT', 'Owner employee full', 1, 1, 1, 1, 1);

    seed_role('R_BM_REPORT', 'REPORT_MANAGEMENT', 'Manager report full', 1, 1, 1, 1, 1);
    seed_role('R_BM_BRANCH', 'BRANCH_MANAGEMENT', 'Manager branch view', 0, 0, 0, 0, 1);
    seed_role('R_BM_CUSTOMER', 'CUSTOMER_MANAGEMENT', 'Manager customer full', 1, 1, 1, 1, 1);
    seed_role('R_BM_EQUIP', 'EQUIPMENT_MANAGEMENT', 'Manager equipment full', 1, 1, 1, 1, 1);
    seed_role('R_BM_PRODUCT', 'PRODUCT_MANAGEMENT', 'Manager product full', 1, 1, 1, 1, 1);
    seed_role('R_BM_EMP', 'EMPLOYEE_MANAGEMENT', 'Manager employee full', 1, 1, 1, 1, 1);

    seed_role('R_CA_CUSTOMER', 'CUSTOMER_MANAGEMENT', 'Cashier customer', 1, 1, 0, 0, 1);
    seed_role('R_CA_EQUIP', 'EQUIPMENT_MANAGEMENT', 'Cashier equipment view', 0, 0, 0, 0, 1);
    seed_role('R_CA_PRODUCT', 'PRODUCT_MANAGEMENT', 'Cashier product view', 0, 0, 0, 0, 1);

    seed_mapping('M_OWN_REPORT', 'OWNER', 'R_OWNER_REPORT');
    seed_mapping('M_OWN_BRANCH', 'OWNER', 'R_OWNER_BRANCH');
    seed_mapping('M_OWN_CUSTOMER', 'OWNER', 'R_OWNER_CUSTOMER');
    seed_mapping('M_OWN_EQUIP', 'OWNER', 'R_OWNER_EQUIP');
    seed_mapping('M_OWN_PRODUCT', 'OWNER', 'R_OWNER_PRODUCT');
    seed_mapping('M_OWN_EMP', 'OWNER', 'R_OWNER_EMP');

    seed_mapping('M_BM_REPORT', 'BRANCH_MANAGER', 'R_BM_REPORT');
    seed_mapping('M_BM_BRANCH', 'BRANCH_MANAGER', 'R_BM_BRANCH');
    seed_mapping('M_BM_CUSTOMER', 'BRANCH_MANAGER', 'R_BM_CUSTOMER');
    seed_mapping('M_BM_EQUIP', 'BRANCH_MANAGER', 'R_BM_EQUIP');
    seed_mapping('M_BM_PRODUCT', 'BRANCH_MANAGER', 'R_BM_PRODUCT');
    seed_mapping('M_BM_EMP', 'BRANCH_MANAGER', 'R_BM_EMP');

    seed_mapping('M_CA_CUSTOMER', 'CASHIER', 'R_CA_CUSTOMER');
    seed_mapping('M_CA_EQUIP', 'CASHIER', 'R_CA_EQUIP');
    seed_mapping('M_CA_PRODUCT', 'CASHIER', 'R_CA_PRODUCT');
END;
/

MERGE INTO USERS u
USING (
    SELECT 'USR_OWNER_001' USER_ID,
           'LE VAN DUY' HOTEN,
           '0919008669' SDT,
           'levanduy3122006@gmail.com' EMAIL
    FROM dual
) s
ON (u.USER_ID = s.USER_ID)
WHEN MATCHED THEN UPDATE SET
                      u.HOTEN = s.HOTEN,
                      u.SDT = s.SDT,
                      u.EMAIL = s.EMAIL,
                      u.IS_DELETED = 0
                      WHEN NOT MATCHED THEN
                  INSERT (USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED)
                  VALUES (s.USER_ID, s.HOTEN, s.SDT, s.EMAIL, NULL, NULL, SYSDATE, 0);

MERGE INTO ACCOUNT a
    USING (
        SELECT 'ACC_OWNER_001' ACCOUNT_ID,
               'USR_OWNER_001' USER_ID,
               '0900000001' USERNAME,
               'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7' PASSWORD_HASH
        FROM dual
    ) s
    ON (a.ACCOUNT_ID = s.ACCOUNT_ID)
    WHEN MATCHED THEN UPDATE SET
        a.USER_ID = s.USER_ID,
        a.USERNAME = s.USERNAME,
        a.PASSWORD_HASH = s.PASSWORD_HASH,
        a.STATUS = 'ACTIVE',
        a.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
            VALUES (s.ACCOUNT_ID, s.USER_ID, s.USERNAME, s.PASSWORD_HASH, 'ACTIVE', SYSDATE, 0);

MERGE INTO ACCOUNT_ROLE_GROUP arg
    USING (
        SELECT 'ARG_OWNER_001' ACCOUNT_ROLE_GROUP_ID,
               'ACC_OWNER_001' ACCOUNT_ID,
               'OWNER' GROUP_ID
        FROM dual
    ) s
    ON (arg.ACCOUNT_ROLE_GROUP_ID = s.ACCOUNT_ROLE_GROUP_ID)
    WHEN MATCHED THEN UPDATE SET
        arg.ACCOUNT_ID = s.ACCOUNT_ID,
        arg.GROUP_ID = s.GROUP_ID,
        arg.IS_DELETED = 0
    WHEN NOT MATCHED THEN
        INSERT (ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED)
            VALUES (s.ACCOUNT_ROLE_GROUP_ID, s.ACCOUNT_ID, s.GROUP_ID, SYSDATE, 0);

COMMIT;