<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>ReportPortal</title>
    <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: OpenSans, sans-serif;
        }

        a, a:active, a:visited {
            color: #39c2d7;
            text-decoration: underline;
        }

        a:hover {
            text-decoration: none;
        }

        .rplogo, .rplogo:hover, .rplogo:active, .rplogo:visited {
            display: inline-block;
            text-decoration: none;
        }

        @media only screen and (max-width: 540px) {
            body {
                margin: 0;
                padding: 0;
            }

            table[class="mainwrapper"] {
                width: 100% !important;
            }

            .rplogo {
                margin-left: 15px;
            }
        }
    </style>
</head>
<body bgcolor="#f9f9f9" topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" link="#39c2d7" alink="#39c2d7"
      vlink="#39c2d7"
      style="font-family: OpenSans, sans-serif;">
<table width="540" border="0" cellspacing="0" cellpadding="0" align="center" class="mainwrapper">
    <tbody>
    <tr>
        <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="logowrapper">
                <tbody>
                <tr>
                    <td height="48">
                        <a class="rplogo" href="http://reportportal.io" target="_blank"
                           style="font-size: 15px; color: #595c5c; font-weight: bold; font-family: 'Roboto', sans-serif;">
                            ReportPortal.io
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="30" class="contentwrapper" bgcolor="#ffffff">
                <tbody>
                <tr>
                    <td align="left" height="170">
                        <table class="content" width="100%" border="0" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                        <#assign rowCounter = 1>
                        <#macro subtypes sbt>
                            <#list sbt as key,value>
                                <#if rowCounter % 2 == 0>
                                <tr bgcolor="#ffffff" style="background-color:#ffffff;">
                                <#else>
                                <tr bgcolor="#f9f9f9" style="background-color:#f9f9f9;">
                                </#if>
                                <td height="25"
                                    style="font-size: 14px; color: #464547; padding-left: 38px; border-width: 0px;">${key.getLongName()}</td>
                                <td width="40" style="font-size: 14px; color: #464547; border-width: 0px;">${value}</td>
                            </tr>
                                <#assign rowCounter++>


                            </#list>
                        </#macro>

                        <#macro maintype name counter>
                            <#if rowCounter % 2 == 0>
                            <tr bg="#ffffff" style="background-color:#ffffff;">
                            <#else>
                            <tr bgcolor="#f9f9f9" style="background-color:#f9f9f9;">
                            </#if>
                            <td height="40" style="font-size: 14px; color: #464547; padding-left: 20px; border-width: 0px;"><b>${name}</b>
                            </td>
                            <td style="font-size: 14px; color: #464547; border-width: 0px;"><b>${counter}</b></td>
                        </tr>
                            <#assign rowCounter++>

                        </#macro>
                        </table>
                        <!-- Launch name and link to Report Portal instance -->
                        <h2 style="font-size: 20px; color: #777777;" align="center">Launch "${name}" #${number} has been
                            finished</h2>
                        <p style="font-size: 14px; color: #777777;">To view it on Report Portal just visit this <a
                                href="${url}" target="_blank">link</a>.</p>
                    <#if tags??>
                        <p style="font-size: 14px; color: #777777;">Tags to launch:
                            <#list tags as name, link>
                                <a href="${link}" target="_blank" style="padding-left:5px;">${name}</a>
                            </#list>
                        </p>
                    </#if>
                        <!-- Launch name, link and description (if exists) -->
                    <#if description??>
                        <p style="font-size: 14px; color: #777777;">Description of launch:<br>${description}</p>
                    </#if>
                        <table width="300" cellspacing="0" cellpadding="0"  style="border: 1px solid #e9e9e9;">
                            <tr>
                                <td>
                                    <table width="300" border="0" cellspacing="0" cellpadding="6" style="border: none; border-collapse: collapse;">
                                        <tbody>
                                        <tr bgcolor="#f5f5f5" style="background: #f5f5f5;">
                                            <td height="40"
                                                style="font-size: 12px; color: #777777; border-bottom: 1px solid #e9e9e9; padding-left: 20px;">
                                                <b>LAUNCH STATISTICS</b></td>
                                            <td width="40"
                                                style="font-size: 12px; color: #777777; border-bottom: 1px solid #e9e9e9;"></td>
                                        </tr>
                                        <tr bgcolor="#ffffff" style="background: #ffffff;">
                                            <td height="40" style="font-size: 14px; color: #464547; padding-left: 20px; border-width: 0px;">
                                                <b>TOTAL</b>
                                            </td>
                                            <td width="40" style="font-size: 14px; color: #464547; border-width: 0px;"><b>${total}</b></td>
                                        </tr>
                                        <tr bgcolor="#f9f9f9" style="background: #f9f9f9;">
                                            <td height="25" style="font-size: 14px; color: #464547; padding-left: 38px; border-width: 0px;">Passed
                                            </td>
                                            <td width="40" style="font-size: 14px; color: #464547; border-width: 0px;">${passed}</td>
                                        </tr>
                                        <tr bgcolor="#ffffff" style="background: #ffffff;">
                                            <td height="25" style="font-size: 14px; color: #464547; padding-left: 38px; border-width: 0px;">Failed
                                            </td>
                                            <td width="40" style="font-size: 14px; color: #464547; border-width: 0px;">${failed}</td>
                                        </tr>
                                        <tr bgcolor="#f9f9f9" style="background: #f9f9f9;">
                                            <td height="25" style="font-size: 14px; color: #464547; padding-left: 38px; border-width: 0px;">
                                                Skipped
                                            </td>
                                            <td width="40" style="font-size: 14px; color: #464547; border-width: 0px;">${skipped}</td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </table>
                        <br>
                        <table width="100%" cellspacing="0" cellpadding="0"  style="border: 1px solid #e9e9e9;">
                            <tr>
                                <td>
                                    <table width="100%" border="0" cellspacing="0" cellpadding="6" style="border: none; border-collapse: collapse;">
                                        <tbody>
                                        <tr bgcolor="#f5f5f5" style="background: #f5f5f5;">
                                            <td height="40"
                                                style="font-size: 12px; color: #777777; border-bottom: 1px solid #e9e9e9; padding-left: 20px;">
                                                <b>LAUNCH DEFECTS</b></td>
                                            <td width="40"
                                                style="font-size: 12px; color: #777777;  border-bottom: 1px solid #e9e9e9;"></td>
                                        </tr>
                                        <!-- PRODUCT BUG bugs section -->
                                        <#assign name="Product Bugs">
                                        <@maintype name="${name}" counter="${productBugTotal}" />
                                        <#if pbInfo??>
                                            <@subtypes sbt=pbInfo/>
                                        </#if>

                                        <!-- AUTOMATION BUG bugs section -->
                                        <#assign name="Automation Bugs">
                                        <@maintype name="${name}" counter="${automationBugTotal}" />
                                        <#if abInfo??>
                                            <@subtypes sbt=abInfo/>
                                        </#if>

                                        <!-- SYSTEM ISSUE bugs section -->
                                        <#assign name="System Issues">
                                        <@maintype name="${name}" counter="${systemIssueTotal}" />
                                        <#if siInfo??>
                                            <@subtypes sbt=siInfo/>
                                        </#if>

                                        <!-- NO DEFECT bugs section -->
                                        <#assign name="No Defects">
                                        <@maintype name="${name}" counter="${noDefectTotal}" />
                                        <#if ndInfo??>
                                            <@subtypes sbt=ndInfo/>
                                        </#if>

                                        <!-- TO INVESTIGATE bugs section -->
                                        <#assign name="To Investigate">
                                        <@maintype name="${name}" counter="${toInvestigateTotal}" />
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <table width="100%" height="82" border="0" cellspacing="0" cellpadding="0" class="linkswrapper">
                <tbody>
                <tr>
                    <td height="82" align="center">
                        <table border="0" cellspacing="0" cellpadding="12" class="linksline" align="center">
                            <tbody>
                            <tr>
                                <td><p style="font-size: 13px; color: #464547;">Keep in touch with us:</p></td>
                                <td><a href="https://github.com/reportportal" target="_blank"><img src="cid:ic-github.png"
                                                                                                   border="0" width="20"
                                                                                                   height="21"
                                                                                                   alt="github"></a>
                                </td>
                                <td><a href="https://www.facebook.com/ReportPortal.io" target="_blank"><img
                                        src="cid:ic-fb.png" border="0" width="18" height="18" alt="facebook"></a></td>
                                <td><a href="http://twitter.com/ReportPortal_io" target="_blank"><img
                                        src="cid:ic-twitter.png" border="0" width="20" height="16" alt="twitter"></a></td>
                                <td><a href="http://youtube.com/c/ReportPortalCommunity"
                                       target="_blank"><img src="cid:ic-youtube.png" border="0" width="20" height="15"
                                                            alt="youtube"></a></td>
                                <td><a href="https://vk.com/reportportal_io" target="_blank"><img src="cid:ic-vk.png"
                                                                                                  border="0" width="21"
                                                                                                  height="12" alt="vk"></a>
                                </td>
                                <td><a href="https://reportportal-slack-auto.herokuapp.com/" target="_blank"><img src="cid:ic-slack.png"
                                                                                                                  border="0" width="18"
                                                                                                                  height="18"
                                                                                                                  alt="slack"></a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#bdc7cc" class="footerline">
                <tbody>
                <tr>
                    <td height="1"><!-- --></td>
                </tr>
                </tbody>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="footerwrapper">
                <tbody>
                <tr>
                    <td align="center" height="52" class="footercontent">
                        <p style="font-size: 11px; line-height: 1.5; color: #6d6d6d"><b>Report Portal Notification
                            Center</b><br>
                            This notification was created automatically. Please don't reply for this e-mail.</p>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>
</body>
</html>