// Sidebar Toggle Codes;

var sidebarOpen = false;
var sidebar = document.getElementById("sidebar");
var sidebarCloseIcon = document.getElementById("sidebarIcon");

function toggleSidebar() {
  if (!sidebarOpen) {
    sidebar.classList.add("sidebar_responsive");
    sidebarOpen = true;
  }
}

function closeSidebar() {
  if (sidebarOpen) {
    sidebar.classList.remove("sidebar_responsive");
    sidebarOpen = false;
  }
}

function calTotal()
{
  var qty = document.forms["form"]["qty"].value;
  var amount = document.forms["form"]["amount"].value;
  var total = document.forms["form"]["TAmount"];

  total.value = qty * amount;
  
}

function validateForm()
{
  var product = document.forms["form"]["Product"].value;
  var qty = document.forms["form"]["qty"].value;
  var amount = document.forms["form"]["amount"].value;
  var total = document.forms["form"]["TAmount"].value;
  var image = document.forms["form"]["image"].value;

  if( product === "" || product === null)
  {
    alert("Product Name must be filled");
        return false;
  }
  if( qty === "" || qty === null)
  {
    alert("Quantity must be filled");
        return false;
  }
  if( amount === "" || amount === null)
  {
    alert("Amount must be filled");
        return false;
  }
  if( total === "" || total === null)
  {
    alert("Total must be filled");
        return false;
  }
  if( image === "" || image === null)
  {
    alert("Image file must inserted");
        return false;
  }

}



