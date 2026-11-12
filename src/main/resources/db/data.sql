-- 演示数据：3 张采购收货单，字段与《金蝶采购收货单数据库 ER 图》一致
-- 只填有业务含义的列，其余列保持 NULL，用于验证全字段表头 + 空值渲染
INSERT INTO t_im_purrecbill
(fid, fbillno, fbillstatus, fbiztime, fcreatorid, fcreatetime, fmodifierid, fmodifytime, fauditorid, fauditdate, fbilltypeid, fsupplierid, forgid, facceptancestatus, fcomment) VALUES
(1001, 'CGSH-20261111-0001', '已审核', TIMESTAMP '2026-11-11 08:00:00', 9001, TIMESTAMP '2026-11-11 09:12:30', 9002, TIMESTAMP '2026-11-11 09:30:00', 9003, TIMESTAMP '2026-11-11 10:00:00', 1, 8001, 100, '已验收', '演示单据：两行物料，含收货箱与附件'),
(1002, 'CGSH-20261111-0002', '已审核', TIMESTAMP '2026-11-11 09:00:00', 9011, TIMESTAMP '2026-11-11 10:35:10', 9012, TIMESTAMP '2026-11-11 10:50:00', 9013, TIMESTAMP '2026-11-11 11:00:00', 1, 8002, 100, '已验收', '演示单据：单行物料'),
(1003, 'CGSH-20261110-0003', '暂存', TIMESTAMP '2026-11-10 15:00:00', 9021, TIMESTAMP '2026-11-10 16:02:00', NULL, NULL, NULL, NULL, 1, 8003, 200, '待验收', '演示单据：无收货箱与附件');

INSERT INTO t_im_purrecbill_l (fid, fcomment, flocaleid, fpkid) VALUES
(1001, '采购收货单', 'zh_CN', 'L-1001'),
(1002, '采购收货单', 'zh_CN', 'L-1002'),
(1003, '采购收货单', 'zh_CN', 'L-1003');

INSERT INTO t_im_purrecbill_lk (fid, fsbillid, fstableid, fsid, fseq, fpkid) VALUES
(1001, 7001, 2001, 7001, 1, 4101),
(1002, 7002, 2001, 7002, 1, 4102);

INSERT INTO t_im_purrecbill_tc (fid, ftbillid, fttableid, fsbillid, fstableid, fsid, ftid) VALUES
(1001, 7101, 3001, 7001, 2001, 7001, 7201),
(1002, 7102, 3001, 7002, 2001, 7002, 7202);

INSERT INTO t_im_purrecbill_wb (fid, foperate, fruleverid, fsbillid, fstableid, fsid, fwritevalue, fseq, fentryid, fruleitemid) VALUES
(1001, '反写', 1, 7001, 2001, 7001, 120.0000, 1, 2001, 8001),
(1002, '反写', 1, 7002, 2001, 7002, 40.0000, 1, 2003, 8002);

INSERT INTO t_im_purrecbill_f (fid, fexchangerate, fistax, fpaymode, fexratetable, finvoicebiztypeid, fpayconditionid, fexratedate, fcurrencyid, fsettlecurrencyid, fsettletypeid) VALUES
(1001, 1.0000, 'Y', '月结', 1, 1, 1, TIMESTAMP '2026-11-11 08:00:00', 1, 1, 1),
(1002, 1.0000, 'Y', '月结', 1, 1, 1, TIMESTAMP '2026-11-11 09:00:00', 1, 1, 1);

INSERT INTO t_im_purrecbillentry
(fentryid, fid, fseq, fmaterialid, fmaterialname, fqty, fbaseqty, fprice, fpriceandtax, famount, famountandtax, fcuramount, ftaxamount, ftaxrate, fwarehouseid, flotnumber, fk_usp_textfield1, fk_usp_deliverybill, fk_usp_inspect_doc_no, fentrycomment) VALUES
(2001, 1001, 1, 5001, '铝合金外壳 A 型', 80.0000, 80.0000, 35.5000, 40.1150, 2840.0000, 3209.2000, 2840.0000, 369.2000, 13.0000, 1, 'BATCH-A01', 'SBOX-0001', 'CGTZ-20261110-0088', 'JY-20261111-0001', '演示明细：含三个箱唛'),
(2002, 1001, 2, 5002, '不锈钢紧固件 M6', 40.0000, 40.0000, 2.8000, 3.1640, 112.0000, 126.5600, 112.0000, 14.5600, 13.0000, 1, 'BATCH-A02', 'SBOX-0002', 'CGTZ-20261110-0088', 'JY-20261111-0002', '演示明细：含一个箱唛'),
(2003, 1002, 1, 5100, '贴片电阻 10K', 40.0000, 40.0000, 0.1200, 0.1356, 4.8000, 5.4240, 4.8000, 0.6240, 13.0000, 3, 'BATCH-C01', 'SBOX-0003', 'CGTZ-20261110-0090', 'JY-20261111-0003', '演示明细'),
(2004, 1003, 1, 5900, '碳纤维预浸料', 12.0000, 12.0000, 260.0000, 293.8000, 3120.0000, 3525.6000, 3120.0000, 405.6000, 13.0000, 5, NULL, NULL, 'CGTZ-20261109-0071', NULL, '演示明细：无箱唛与附件');

INSERT INTO t_im_purrecbillentry_l (fprovideraddress, flocaleid, fpkid, fentryid) VALUES
('上海市浦东新区', 'zh_CN', 'EL-2001', 2001),
('上海市浦东新区', 'zh_CN', 'EL-2002', 2002),
('深圳市南山区', 'zh_CN', 'EL-2003', 2003),
('北京市朝阳区', 'zh_CN', 'EL-2004', 2004);

INSERT INTO t_im_purrecbillentry_r (fentryid, finvqty, fverifyqty, fpurorderbillnumber, fsrcbillnumber, fmainbillnumber, facceptancedate, fqualifiedqty) VALUES
(2001, 80.0000, 80.0000, 'CGDD-20261101-0012', 'CGTZ-20261110-0088', 'CGSH-20261111-0001', TIMESTAMP '2026-11-11 10:00:00', 80.0000),
(2002, 40.0000, 40.0000, 'CGDD-20261101-0012', 'CGTZ-20261110-0088', 'CGSH-20261111-0001', TIMESTAMP '2026-11-11 10:00:00', 40.0000),
(2003, 40.0000, 40.0000, 'CGDD-20261101-0015', 'CGTZ-20261110-0090', 'CGSH-20261111-0002', TIMESTAMP '2026-11-11 11:00:00', 40.0000),
(2004, 0.0000, 0.0000, 'CGDD-20261101-0020', 'CGTZ-20261109-0071', 'CGSH-20261110-0003', NULL, 0.0000);

INSERT INTO t_im_purrecbillentry_lk (fbaseqty_old, fk_usp_qtyfield2, fk_usp_baseunitqtyfield2, fsbillid, fk_usp_baseunitqtyfield2_old, fstableid, fsid, fseq, fk_usp_qtyfield2_old, fbaseqty, fentryid, fpkid) VALUES
(80.0000, NULL, NULL, 7001, NULL, 2001, 7001, 1, NULL, 80.0000, 2001, 4001),
(40.0000, NULL, NULL, 7001, NULL, 2001, 7001, 2, NULL, 40.0000, 2002, 4002),
(40.0000, NULL, NULL, 7002, NULL, 2001, 7002, 1, NULL, 40.0000, 2003, 4003);

INSERT INTO tk_usp_receivingboxentry
(fk_usp_sendboxno, fk_usp_wxno, fk_usp_srcsubentryid, fseq, fk_usp_arrived, fk_usp_realqty, fdetailid, fk_usp_detailqty, fentryid) VALUES
('BOX-0001', 'CONT-A01', 7001, 1, '是', 30.0000, 3001, 30.0000, 2001),
('BOX-0002', 'CONT-A01', 7001, 2, '是', 30.0000, 3002, 30.0000, 2001),
('BOX-0003', 'CONT-A02', 7001, 3, '否', 20.0000, 3003, 20.0000, 2001),
('BOX-0004', 'CONT-B01', 7002, 1, '是', 40.0000, 3004, 40.0000, 2002),
('BOX-0005', 'CONT-C01', 7003, 1, '是', 40.0000, 3005, 40.0000, 2003);

INSERT INTO tk_usp_cgshentryattach
(fk_usp_is_special, fk_usp_fileid, fk_usp_download_link, fk_usp_checkboxfield, fk_usp_isfolder, fseq, fdetailid, fk_usp_isupload, fentryid, fk_usp_attachname, fk_usp_pre_link) VALUES
('否', 6001, '/attach/download/5001', '是', '否', 1, 5001, '是', 2001, '材质证明书', '/attach/preview/5001'),
('否', 6002, '/attach/download/5002', '是', '否', 2, 5002, '是', 2001, '送货单扫描件', '/attach/preview/5002'),
('是', 6003, '/attach/download/5003', '是', '否', 1, 5003, '是', 2002, 'RoHS 报告', '/attach/preview/5003'),
('否', 6004, '/attach/download/5004', '否', '否', 1, 5004, '否', 2003, '包装清单', '/attach/preview/5004');

INSERT INTO tk_usp_cgentryattachment (fbasedataid, fdetailid, fpkid) VALUES
(6001, 5001, 6001),
(6002, 5001, 6002),
(6003, 5002, 6003),
(6004, 5003, 6004);
