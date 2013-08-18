<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<link rel="stylesheet" href="./css/throttling.css" type="text/css"/>

<html>

<head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
   <title><tiles:insertAttribute name="title" ignore="true"/></title>
</head>

<body>
   
   <div class="wrapper">
      <table cellpadding="2" cellspacing="2" align="center" width="100%" height="100%">
         <tr>
            <td colspan="2" align="center"><tiles:insertAttribute name="header"/></td>
         </tr>
         <tr>
            <td width="15%" valign="top"><tiles:insertAttribute name="menu"/></td>
            <td width="85%" valign="top"><tiles:insertAttribute name="body"/></td>
         </tr>
         <tr>
            <td><div class="push"></div></td>
         </tr>
      </table>
   </div>

   <!-- http://ryanfait.com/resources/footer-stick-to-bottom-of-page/ -->
   <div class="footer">
      <tiles:insertAttribute name="footer"/>
   </div>

</body>

</html>