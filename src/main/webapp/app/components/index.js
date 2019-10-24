
require('./address/AddressDirective');
require('./navbar/NavigationController');
require('./request-type/RequestTypeDirective');
require('./sidebar/LabRequestsSidebarDirective');
require('./sidebar/SidebarDirective');
require('./smart-table/stPersistedSearch');
require('./specialism/SpecialismDirective');

export const templates = {
    'app/components/footer/footer.html': require('./footer/footer.html'),
    'app/components/navbar/navbar.html': require('./navbar/navbar.html'),
    'app/components/navbar/navbar-login.html': require('./navbar/navbar-login.html'),
    'app/components/smart-table/pagination.html': require('./smart-table/pagination.html')
};
