<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" style="height: 100%;">
    <head>
        <LINK REL="stylesheet" HREF="errorPage.css" TYPE="text/css">
        <title>LTI Error Page</title>
    </head>
    <body class="bodyPanel" style="background-image: linear-gradient(transparent, rgba(255,0,0,0.6)), url('${backgroundUrl}')">
        <div class="backgroundBody">
            <div class="imageDiv"><img src="${logoUrl}" class="imageStyle"></img></div>
            <div class="headerText">Well, This is a Problem...</div>
            <div class="errorTitle">${title}</div>
            <div class="errorMessage">${message}</div>
            <%-- Only show details if it was provided.  --%>
            <c:choose>
                <c:when test="${not empty details}">
                     <div class="errorDetails">DETAILS: ${details}</div>
                </c:when>
            </c:choose>
        </div>
    </body>
</html>