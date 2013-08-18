<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript">
function submitForm() {
   var hidden = document.getElementById("update_simulatedservice_config_submitType");
   hidden.value = "changedFeature";
   document.simulatedservice_config_form.submit();
}
</script>

<div id="content_header">Backend Service Configuration / List / Update (<s:label value="%{nameBackendService}" theme="simple"/>) / Simulated Service / 
Update (<s:label value="%{nameSimulatedService}" theme="simple"/>)</div>

<s:form action="update_simulatedservice_config.action" method="post" name="simulatedservice_config_form" theme="simple">
   <s:hidden name="nameBackendService"/>
   <s:hidden name="nameSimulatedService"/>
   <s:hidden value="Save" name="submitType" id="update_simulatedservice_config_submitType"/>
   
   <table class="edit_background">
      <tr>
         <td>Backend service:</td>
         <td>
            <b><s:label value="%{nameBackendService}"/></b>
         </td>
      </tr>
      <tr>
         <td>Simulated service:</td>
         <td>
            <b><s:label value="%{nameSimulatedService}"/></b>
         </td>
      </tr>      
      <tr>
         <td colspan="2"><s:include value="/jsp/feature_update.jsp"/></td>
      </tr>
      <tr>
         <td colspan="2">
            <font color="red"><s:fielderror/></font>
         </td>
      </tr>
      <tr>
         <td colspan="2">
            <s:submit name="buttonName" value="Save" align="center" theme="simple"/>
            <s:submit name="buttonName" value="Cancel" align="center" theme="simple"/>
         </td>
      </tr>
   </table>
   
</s:form>