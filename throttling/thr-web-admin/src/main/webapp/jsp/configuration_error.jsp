<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="!exception.loaded">
   <h4>The configuration data has not been loaded yet!</h4>
   <br>
   Please load it in Administration / (Re)load the configuration menu.
   </s:if>
<s:else>
   <h4>Error in the configuration file!</h4>

   <b>Exception:</b> <s:property value="exception"/>
   <br><br>
   <b>Exception Details:</b> <s:property value="exceptionStack"/>
</s:else>
