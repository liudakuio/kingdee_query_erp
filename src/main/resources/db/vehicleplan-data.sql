-- 采购车辆计划演示数据（内置 H2，仅用于 demo 档案）
-- 单据 1：VP-20261113-0001（2 行物料信息 / 3 个箱件 / 1 条车辆明细）
-- 单据 2：VP-20261113-0002（1 行物料信息 / 1 个箱件 / 1 条车辆明细）
INSERT INTO tk_usp_vehicleplan
(fid, fbillno, fbillstatus, fk_usp_businesstype, fk_usp_source, fk_usp_destination, fk_usp_origin, fk_usp_starting,
 fk_usp_transportnumber, fk_usp_delivernoticeno, fk_usp_receivedept, forgid, fk_usp_kilometer, fk_usp_combofield,
 fcreatorid, fcreatetime, fmodifierid, fmodifytime, sauditorid, frauddate, fk_usp_billtype, fk_usp_textfield2,
 fk_usp_textfield4, fk_usp_radiooptgroupfield, fk_usp_resubmit, fk_usp_kilometers) VALUES
(9001, 'VP-20261113-0001', 'C', '整车运输', '宁夏银川', '江苏无锡', '宁夏银川', '银川物流中心',
 'YS-20261113-001', 'SHTZ-251231-000003', 5001, 100, 1860.0000, '整车-常规',
 9001, TIMESTAMP '2026-11-13 08:30:00', 9002, TIMESTAMP '2026-11-13 09:10:00', 9003, TIMESTAMP '2026-11-13 09:20:00',
 'CGS-01', '演示：整车发运', '', '否', '否', 1860.0000),
(9002, 'VP-20261113-0002', 'C', '整车运输', '山东青岛', '江苏无锡', '山东青岛', '青岛港',
 'YS-20261113-002', 'SHTZ-251231-000004', 5001, 100, 820.0000, '整车-加急',
 9004, TIMESTAMP '2026-11-13 10:00:00', 9004, TIMESTAMP '2026-11-13 10:15:00', 9003, TIMESTAMP '2026-11-13 10:25:00',
 'CGS-01', '演示：加急发运', '', '否', '否', 820.0000);

INSERT INTO tk_usp_vehicleplan_tc (fid, ftbillid, fttableid, fsbillid, fstableid, fsid, ftid) VALUES
(9001, 9101, 9001, 8001, 2001, 8001, 7001);

INSERT INTO tk_usp_container_entry
(fentryid, fid, fseq, fk_usp_itemnum, fk_usp_devicename, fk_usp_equipspecification, fk_usp_material,
 fk_usp_qty, fk_usp_baseqty, fk_usp_vehicleplan, fk_usp_deliverynoticeno, fk_usp_supplysendnum,
 fk_usp_transportnumber, fk_usp_warehouse, fk_usp_unit, fk_usp_lotnumber, fk_usp_do, fk_usp_logisticsstatus) VALUES
(9101, 9001, 1, 'MAT-001', '工业硅粉', 'Si 98.5%', 6001, 25.0000, 25000.0000, 'VP-20261113-0001', 'SHTZ-251231-000003', 'SBOX-0001', 'YS-20261113-001', 1, 1, 'BATCH-A01', '否', '待发运'),
(9102, 9001, 2, 'MAT-002', '多晶硅料', 'Si 99.9%', 6002, 40.0000, 40000.0000, 'VP-20261113-0001', 'SHTZ-251231-000003', 'SBOX-0002', 'YS-20261113-001', 1, 1, 'BATCH-A02', '否', '待发运'),
(9103, 9002, 1, 'MAT-003', '石墨电极', 'HP 450mm', 6003, 12.0000, 1200.0000, 'VP-20261113-0002', 'SHTZ-251231-000004', 'SBOX-0003', 'YS-20261113-002', 2, 1, 'BATCH-B01', '是', '已发运');

INSERT INTO tk_usp_subentryentity
(fdetailid, fentryid, fseq, fk_usp_sendboxno, fk_usp_wxno, fk_usp_volume, fk_usp_gross_weight, fk_usp_net_weight,
 fk_usp_height, fk_usp_width, fk_usp_length, fk_usp_detailnum, fk_usp_detailqty, fk_usp_associatedqty,
 fk_usp_arrived, fk_usp_issh, fk_usp_suplot) VALUES
(9201, 9101, 1, 'BOX-0001', 'CONT-A01', '1.20', '250', '240', '80', '60', '40', 10.0000, 10.0000, 10.0000, '是', '否', 'BATCH-A01'),
(9202, 9101, 2, 'BOX-0002', 'CONT-A01', '1.20', '250', '240', '80', '60', '40', 15.0000, 15.0000, 15.0000, '是', '否', 'BATCH-A01'),
(9203, 9102, 1, 'BOX-0003', 'CONT-B01', '1.60', '420', '400', '90', '70', '50', 40.0000, 40.0000, 40.0000, '否', '否', 'BATCH-A02'),
(9204, 9103, 1, 'BOX-0004', 'CONT-C01', '0.90', '180', '170', '70', '50', '30', 12.0000, 12.0000, 12.0000, '是', '否', 'BATCH-B01');

INSERT INTO tk_usp_vehicleentry
(fentryid, fid, fseq, fk_usp_carrier, fk_usp_telephonefield, fk_usp_wxnos, fk_usp_outgostate, fk_usp_outgotime,
 fk_usp_incomestate, fk_usp_incometime, fk_usp_arrivedate, fk_usp_driverpicturer, fk_usp_transportsupplier,
 fk_usp_modifierfield, fk_usp_textfield, fk_usp_textfield1, fk_usp_textfield3) VALUES
(9301, 9001, 1, '顺通物流', '13800000001', 'CONT-A01', '已出库', TIMESTAMP '2026-11-13 11:00:00',
 '未入库', NULL, TIMESTAMP '2026-11-13 16:00:00', 'driver-01.jpg', 7001, 9002, '', '', ''),
(9302, 9002, 1, '安捷运输', '13800000002', 'CONT-C01', '已出库', TIMESTAMP '2026-11-13 11:30:00',
 '已入库', TIMESTAMP '2026-11-13 17:00:00', TIMESTAMP '2026-11-13 16:30:00', 'driver-02.jpg', 7002, 9004, '', '', '');