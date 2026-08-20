<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>订单确认 - ${orderId}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }

        .order-detail {
            background-color: #f5f5f5;
            padding: 20px;
        }

        .product-table {
            width: 100%;
            border-collapse: collapse;
        }

        .product-table th, .product-table td {
            border: 1px solid #ddd;
            padding: 8px;
        }

        .product-table th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
<h2>尊敬的 ${userName}，您的订单已确认！</h2>

<div class="order-detail">
    <p><strong>订单编号：</strong> ${orderId}</p>
    <p><strong>订单日期：</strong> ${orderDate}</p>

    <h3>商品明细：</h3>
    <table class="product-table">
        <tr>
            <th>商品名称</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
        </tr>
        <#list products as product>
            <tr>
                <td>${product.name}</td>
                <td>${product.price}</td>
                <td>${product.quantity}</td>
                <td>${product.quantity * product.price}</td>
            </tr>
        </#list>
        <tr>
            <td colspan="3" align="right"><strong>总计：</strong></td>
            <td><strong>${totalAmount}</strong></td>
        </tr>
    </table>
</div>

<p>感谢您的购买，如有任何问题请联系客服。</p>

<hr>
<footer>
    <p style="color: #666; font-size: 12px;">
        此为系统自动发送邮件，请勿回复。
    </p>
</footer>
</body>
</html>