<div>
    <#if description??>
        ${description}
    </#if>

<#--if RP contains some comments for test item-->
    <#if comments??>
		<h3>Test Item comments:</h3>
        ${comments}
    </#if>

<#--##  if backlinks are present-->
    <#if backLinks??>
        <h3>Back link to Report Portal:</h3>
        <ul type="square">
        <#list backLinks as key,value>
            <li><a href="${value}" rel="nofollow" title="Follow link">Link to defect</a></li>
        </#list>
        </ul>
        <br>
    </#if>

<#--Complex block with logic in velocity. Consider to move all logic in JAVA code.-->
        <#if logs?? && logs?size != 0>
        <div class="panel">
            <div class="panelHeader"
                 style="border-bottom-width: 1px;border-bottom-style: solid;border-bottom-color: #ccc;background-color: #6DB33F;color: #34302D;">
                <b>Test execution log</b>
            </div>
            <div class="panelContent">
                <#list logs as logEntry>
                    <#if logEntry.message??>
                        <div style="border-width: 1px;">
                            <pre style="white-space: pre-wrap; display: block;font-family: monospace;max-height: 30em;text-align: left;">${logEntry.message}</pre>
                        </div>
                    </#if>
                    <#if logEntry.image>
                        HALLLOOOOO
                    </#if>
                <#--##    if URL provided to screen source-->
                    <#if logEntry.decodedFileName??>
                        <#if (logEntry.image)!false>
                         <p><img src="${logEntry.decodedFileName}" align="absmiddle" border="0" height="366"><br class="atl-forced-newline">
                         </p>
                        <#else>
                        <p>
                            <a href="${logEntry.decodedFileName}">
                                "${logEntry.decodedFileName}"
                            </a>
                            <br class="atl-forced-newline">
                        </p>
                        </#if>
                    </#if>
                </#list>
            </div>
        </div>
        </#if>
</div>
