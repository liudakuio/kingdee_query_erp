-- 若依（RuoYi-Vue）菜单脚本，MySQL 版本
-- 目录 path=kingdee + 菜单 path=receive → 前端路由 /kingdee/receive
-- 组件 kingdee/receive/index 对应 ruoyi-ui/src/views/kingdee/receive/index.vue

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, is_frame, is_cache,
  menu_type, visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  '金蝶ERP', 0, 5, 'kingdee', NULL, 1, 0,
  'M', '0', '0', '', 'el-icon-s-shop', 'admin', sysdate(), '金蝶 ERP 数据查询目录'
);

SET @kingdee_dir_id = LAST_INSERT_ID();

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, is_frame, is_cache,
  menu_type, visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  '采购收货单查询', @kingdee_dir_id, 1, 'receive', 'kingdee/receive/index', 1, 0,
  'C', '0', '0', 'kingdee:receive:query', 'el-icon-document', 'admin', sysdate(),
  '按单据编号查询采购收货单主表、明细、收货箱与附件'
);

-- 授权给普通角色（若依默认普通角色 role_id = 2）时可执行：
-- INSERT INTO sys_role_menu(role_id, menu_id)
-- SELECT 2, menu_id FROM sys_menu WHERE perms = 'kingdee:receive:query' OR (menu_name = '金蝶ERP' AND parent_id = 0);

-- 不同若依版本 sys_menu 字段可能略有差异（如缺少 menu_name 之外的列），
-- 执行前请对照实际表结构调整列名。Oracle / PostgreSQL 版本请自行替换 sysdate() 与 LAST_INSERT_ID()。
