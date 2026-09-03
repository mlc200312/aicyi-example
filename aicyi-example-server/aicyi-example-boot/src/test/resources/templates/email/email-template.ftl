<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>入职申请通知</title>
</head>
<body style="margin:0;padding:0;background-color:#f5f7fa;font-family:Arial,Helvetica,sans-serif;color:#262626;">

<table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f5f7fa;padding:24px 12px;">
    <tr>
        <td align="center">

            <table width="100%" cellpadding="0" cellspacing="0" border="0"
                   style="max-width:680px;background:#ffffff;border-radius:16px;padding:40px 32px;box-sizing:border-box;">

                <!-- Header -->
                <tr>
                    <td align="center" style="padding-bottom:32px;">
                        <div style="font-size:28px;font-weight:700;color:#1677ff;line-height:40px;">
                            入职申请通知
                        </div>
                        <div style="margin-top:12px;font-size:14px;color:#8c8c8c;line-height:22px;">
                            Employee Onboarding Application
                        </div>
                    </td>
                </tr>

                <!-- Greeting -->
                <tr>
                    <td style="font-size:16px;line-height:30px;color:#262626;">
                        尊敬的 HR 您好：
                        <br/><br/>
                        以下为员工提交的入职申请信息，请及时审核处理。
                    </td>
                </tr>

                <!-- Applicant Info -->
                <tr>
                    <td style="padding-top:32px;">

                        <table width="100%" cellpadding="0" cellspacing="0" border="0"
                               style="border:1px solid #e5e6eb;border-radius:12px;padding:24px;background:#fafafa;">

                            <tr>
                                <td colspan="2" style="font-size:18px;font-weight:600;color:#262626;padding-bottom:20px;">
                                    申请人信息
                                </td>
                            </tr>

                            <tr>
                                <td style="width:140px;padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    姓名
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{employeeName\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    手机号
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{phoneNumber\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    邮箱
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;word-break:break-all;">
                                    \$\{email\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    应聘岗位
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{jobTitle\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    所属部门
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{departmentName\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    入职日期
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{onboardingDate\}
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:10px 0;font-size:14px;color:#8c8c8c;vertical-align:top;">
                                    工作地点
                                </td>
                                <td style="padding:10px 0;font-size:14px;color:#262626;">
                                    \$\{workLocation\}
                                </td>
                            </tr>

                        </table>

                    </td>
                </tr>

                <!-- Attachment -->
                <tr>
                    <td style="padding-top:28px;">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0"
                               style="border:1px solid #e5e6eb;border-radius:12px;padding:20px;">

                            <tr>
                                <td style="font-size:18px;font-weight:600;color:#262626;padding-bottom:16px;">
                                    附件资料
                                </td>
                            </tr>

                            <tr>
                                <td style="font-size:14px;color:#262626;line-height:28px;">
                                    <#if attachments?? && (attachments?size > 0)>
                                        <#list attachments as attachment>
                                            • ${attachment}
                                            <br/>
                                        </#list>
                                    <#else>
                                        暂无附件资料
                                    </#if>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>

                <!-- Remark -->
                <tr>
                    <td style="padding-top:28px;">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0"
                               style="background:#f0f7ff;border-radius:12px;padding:20px;">
                            <tr>
                                <td style="font-size:14px;line-height:28px;color:#595959;">
                                    请 HR 在收到邮件后尽快完成入职审核，并通知相关部门准备办公设备、账号权限及工位安排。
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <!-- Button -->
                <tr>
                    <td align="center" style="padding-top:36px;">
                        <a href="\$\{detailUrl\}"
                           style="display:inline-block;background:#1677ff;color:#ffffff;text-decoration:none;padding:14px 36px;border-radius:999px;font-size:16px;font-weight:600;">
                            查看申请详情
                        </a>
                    </td>
                </tr>

                <!-- Footer -->
                <tr>
                    <td style="padding-top:40px;font-size:12px;line-height:24px;color:#8c8c8c;text-align:center;">
                        此邮件由系统自动发送，请勿直接回复。
                        <br/>
                        © 2026 Company HR System. All Rights Reserved.
                    </td>
                </tr>

            </table>

        </td>
    </tr>
</table>

</body>
</html>
