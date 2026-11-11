-- 演示数据：3 张采购收货单，覆盖「有箱唛/附件」「无子表数据」等场景
INSERT INTO t_im_purrecbill VALUES
(1001, 'CGSH-20261111-0001', DATE '2026-11-11', '已审核', '华东精密制造有限公司', '张伟', TIMESTAMP '2026-11-11 09:12:30', '演示单据：两行物料，含收货箱与附件'),
(1002, 'CGSH-20261111-0002', DATE '2026-11-11', '已审核', '南方电子元件有限公司', '李娜', TIMESTAMP '2026-11-11 10:35:10', '演示单据：单行物料'),
(1003, 'CGSH-20261110-0003', DATE '2026-11-10', '暂存', '北方新材料有限公司', '王强', TIMESTAMP '2026-11-10 16:02:00', '演示单据：无收货箱与附件');

INSERT INTO t_im_purrecbill_l VALUES (1001, 'zh_CN', '采购收货单'), (1002, 'zh_CN', '采购收货单'), (1003, 'zh_CN', '采购收货单');
INSERT INTO t_im_purrecbill_lk VALUES (1001, 'CGTZ-20261110-0088', '收货通知单'), (1002, 'CGTZ-20261110-0090', '收货通知单');
INSERT INTO t_im_purrecbill_tc VALUES (1001, 'TRK-20261111-0001', '已入库'), (1002, 'TRK-20261111-0002', '运输中');
INSERT INTO t_im_purrecbill_wb VALUES (1001, 'CGDD-20261101-0012', 120.0000, TIMESTAMP '2026-11-11 09:20:00'), (1002, 'CGDD-20261101-0015', 40.0000, TIMESTAMP '2026-11-11 10:40:00');
INSERT INTO t_im_purrecbill_f VALUES (1001, 'VMI-01', '华东一仓'), (1002, 'VMI-02', '华南二仓');

INSERT INTO t_im_purrecbillentry VALUES
(2001, 1001, 1, 'MAT-001', '铝合金外壳 A 型', 80.0000, 'WH-01', 'CGTZ-20261110-0088', 'JY-20261111-0001'),
(2002, 1001, 2, 'MAT-002', '不锈钢紧固件 M6', 40.0000, 'WH-01', 'CGTZ-20261110-0088', 'JY-20261111-0002'),
(2003, 1002, 1, 'MAT-100', '贴片电阻 10K', 40.0000, 'WH-03', 'CGTZ-20261110-0090', 'JY-20261111-0003'),
(2004, 1003, 1, 'MAT-900', '碳纤维预浸料', 12.0000, 'WH-05', 'CGTZ-20261109-0071', NULL);

INSERT INTO t_im_purrecbillentry_l VALUES
(2001, 'zh_CN', '铝合金外壳 A 型'), (2002, 'zh_CN', '不锈钢紧固件 M6'),
(2003, 'zh_CN', '贴片电阻 10K'), (2004, 'zh_CN', '碳纤维预浸料');
INSERT INTO t_im_purrecbillentry_r VALUES
(2001, 80.0000, 35.5000, 2840.0000, 13.0000),
(2002, 40.0000, 2.8000, 112.0000, 13.0000),
(2003, 40.0000, 0.1200, 4.8000, 13.0000),
(2004, 12.0000, 260.0000, 3120.0000, 13.0000);
INSERT INTO t_im_purrecbillentry_lk VALUES
(4001, 2001, 'CGTZ-20261110-0088-1', 80.0000),
(4002, 2002, 'CGTZ-20261110-0088-2', 40.0000),
(4003, 2003, 'CGTZ-20261110-0090-1', 40.0000);

INSERT INTO tk_usp_receivingboxentry VALUES
(3001, 2001, 'BOX-0001', 30.0000, 'BATCH-A01', '已收货'),
(3002, 2001, 'BOX-0002', 30.0000, 'BATCH-A01', '已收货'),
(3003, 2001, 'BOX-0003', 20.0000, 'BATCH-A02', '待检验'),
(3004, 2002, 'BOX-0004', 40.0000, 'BATCH-B01', '已收货'),
(3005, 2003, 'BOX-0005', 40.0000, 'BATCH-C01', '已收货');

INSERT INTO tk_usp_cgshentryattach VALUES
(5001, 2001, '材质证明书', '质检资料', '是', '合格'),
(5002, 2001, '送货单扫描件', '随货单据', '是', '合格'),
(5003, 2002, 'RoHS 报告', '质检资料', '是', '合格'),
(5004, 2003, '包装清单', '随货单据', '否', '待补');

INSERT INTO tk_usp_cgentryattachment VALUES
(6001, 5001, 'material-cert-001.pdf', '/attach/2026/11/material-cert-001.pdf', 235621, TIMESTAMP '2026-11-11 09:15:00'),
(6002, 5001, 'material-cert-001-sign.jpg', '/attach/2026/11/material-cert-001-sign.jpg', 88912, TIMESTAMP '2026-11-11 09:16:00'),
(6003, 5002, 'delivery-note-0088.pdf', '/attach/2026/11/delivery-note-0088.pdf', 154320, TIMESTAMP '2026-11-11 09:18:00'),
(6004, 5003, 'rohs-2026.pdf', '/attach/2026/11/rohs-2026.pdf', 456700, TIMESTAMP '2026-11-11 10:38:00');
