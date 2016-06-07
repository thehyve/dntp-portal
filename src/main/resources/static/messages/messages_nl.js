/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(hasModuleExports, hasWindow) {
    'use strict';

    var _messages_nl = {
        /* ========= */
        /* Global */
        /* ========= */

        'Management': 'Beheer',
        'Source': 'Bron',
        'The password has been successfully changed.': 'Het wachtwoord is succesvol gewijzigd.',
        'There is a problem logging in.': 'Er is een probleem met inloggen.',
        'User is not associated with a pathology laboratory.': 'Deze gebruiker is niet verbonden aan een pathologielaboratorium.',
        'Search': 'Zoeken',
        'Lab requests': 'Labverzoeken',
        'About': 'Over',
        'Username': 'Gebruikersnaam',
        'Users': 'Gebruikers',
        'User management': 'Gebruikersbeheer',
        'Labs': 'Laboratoria',
        'Laboratory management': 'Laboratoriumbeheer',
        'Passwords do not match.': 'De wachtwoorden zijn niet gelijk.',
        'Thank you': 'Bedankt',
        'Registration completed. An email with an activation link was sent to ?.': 'Registratie is afgerond. Een e-mail met de activatielink is gestuurd naar {{email}}.',
        'Password must be 8 or more characters long, contain numbers, letters, and a special character.': 'Wachtwoord moet ten minste 8 tekens lang zijn en cijfers, letters en een speciaal teken bevatten.',

        /* ========= */
        /* Request */
        /* ========= */
        'Principal investigator': 'Projectleider',
        'popover: Principal investigator': 'Projectleider is de eindverantwoordelijk onderzoeker.',
        'Principal investigator email': 'E-mail projectleider',
        'popover: Principal investigator email': 'E-mailadres projectleider.',
        'popover: Pathologist': 'De patholoog betrokken bij dit onderzoek. ',
        'Pathologist': 'Patholoog',
        'popover: Pathologist email': 'E-mailadres patholoog.',
        'Postal code': 'Postcode',
        'popover: Postal code': 'De postcode.',
        'City': 'Plaats',
        'popover: City': 'De plaats.',
        'Address': 'Adres',
        'popover: Address': 'Postadres waar factuur naar verzonden mag worden. ',
        'popover: E-mail': 'E-mail crediteurenadministratie.',
        'popover: Telephone': 'Het telefoonnummer.',
        'Billing address': 'Facturatieadres',
        'Billing details': 'Facturatiedetails',
        'Charge number': 'Kostenplaats',
        'popover: Charge number': 'Kostenplaats van het project.',
        'Grant number': 'Subsidienummer',
        'popover: Research number': 'Projectnummer van subsidieverstrekker.',
        'Previous contact': 'Vorig contact',
        'popover: Previous contact': 'Heeft u contact gehad met PALGA over deze aanvraag?',
        'Did you contact Palga previously?': 'Heeft u contact gehad met PALGA over deze aanvraag?',
        'popover: Description of previous contact': 'Met wie heeft u contact gehad en wat is besproken?',
        'Description of previous contact.': 'Beschrijf het contact.',
        'Yes': 'Ja',
        'No': 'Nee',
        'Running title': 'Titel onderzoeksproject',
        'popover: Title': 'De titel van het onderzoek.',
        'Background': 'Achtergrond',
        'popover: Background': 'Achtergrond van het onderzoek.',
        'Research question': 'Onderzoeksvraag',
        'popover: Research question': 'De onderzoeksvraag of -vragen.',
        'Hypothesis': 'Hypothese',
        'popover: Hypothesis': 'De hypothese.',
        'Methods': 'Methode',
        'popover: Methods': 'Welke technieken (bijv. kiembaanmutatieanalyse/moleculaire technieken, immuunhistochemie, tissue micro array enz.) worden gebruikt om tot beantwoording van de hypothese te komen? Wat is het minimaal aantal patiënten dat nodig is om deze vraag te kunnen beantwoorden?',
        'Request type': 'Benodigde data en/of materialen',
        'popover: Request type': 'Benodigde data en/of materialen. Voor uitleg over PALGA-gegevens, bijv. welke gegevens een excerpt bevat, zie <a target="_blank" tabindex="-1" href="http://www.palga.nl/gegevensaanvragen/excerpten.html">www.palga.nl/gegevensaanvragen/excerpten.html</a>.',

        'Numbers only, exploratory request': 'Alleen aantallen, oriënterende zoekvraag',
        'Excerpts only': 'Alleen excerpten',
        'Excerpts and full PA reports': 'Excerpten en complete PA-verslagen',
        'Excerpts and PA material': 'Excerpten en PA-materiaal',
        'Excerpts, PA reports and PA material': 'Excerpten, complete PA-verslagen en PA-materiaal',
        'Full PA reports only (no excerpts)': 'Alleen PA-verslagen',
        'PA material only': 'Alleen PA-materiaal',
        'Excerpts, reports, material or clinical data': 'Excerpten, PA-verslagen, PA-materiaal of klinische gegevens',
        'Excerpts': 'Excerpten',
        'PA reports': 'Complete PA-verslagen',
        'PA material': 'PA-materiaal',
        'Clinical data': 'Klinische gegevens',

        'Request_Type_1': 'Alleen aantallen, oriënterende zoekvraag',
        'Request_Type_2': 'Alleen excerpten',
        'Request_Type_3': 'Excerpten en complete PA-verslagen',
        'Request_Type_4': 'Excerpten en PA-materiaal',
        'Request_Type_5': 'Excerpten, complete PA-verslagen en PA-materiaal',
        'Request_Type_6': 'Alleen PA-verslagen',
        'Request_Type_7': 'Alleen PA-materiaal',

        'Upload file': 'Upload bestand(en)',
        'drag and drop your file here': 'sleep bestanden hierheen',
        'No attachments uploaded.': 'Nog geen attachments geüpload.',
        'Letter of medical research ethics committee': 'Brief van medisch-ethische toetsingscommissie',
        'Upload letter': 'Upload brief',
        'Submit': 'Indienen',
        'Save': 'Opslaan',
        'Cancel': 'Annuleren',
        'Are you sure you want to delete file ?': 'Weet u zeker dat u het bestand {{name}} wilt verwijderen?',
        'Maximum file size ? MB.': 'Maximum bestandsgrootte {{mb_max}} MB.',
        'File too large': 'Het bestand is te groot ({{mb_size}} MB). Het maximum is {{mb_max}} MB.',
        'Are you sure you want to approve the selection?<br>After approving, lab requests will be generated.' :
            'Weet je zeker dat je de selectie wilt goedkeuren?<br>Na goedkeuring worden de labverzoeken aangemaakt.',
        'Are you sure you want to delete request ?': 'Weet u zeker dat u aanvraag {{id}} wilt verwijderen?',
        'Are you sure you want to submit the request? After submission the request cannot be edited anymore.' :
            'Weet u zeker dat u de aanvraag wilt indienen? Na het indienen kan de aanvraag niet meer bewerkt worden.',
        'Are you sure you want to send the request to the scientific council?': 'Weet je zeker dat je de aanvraag naar de wetenschappelijk raad wilt sturen?',
        'Are you sure you want to finalise the request?': 'Weet u zeker dat u de aanvraag wilt afronden?',
        'Are you sure you want to close the request?<br>After closing, no data files can be added.': 'Weet je zeker dat je de aanvraag wilt afsluiten?<br>Na het sluiten kunnen geen bestanden meer worden toegevoegd.',
        'Are you sure you want to reject the request?': 'Weet je zeker dat je de aanvraag wilt afwijzen?',
        'Please enter the reason for rejection.': 'Vul de reden van afwijzing in.',
        'Are you sure you want to reject the selection?<br>After rejecting, the status will return to \'Data delivery.\'' :
            'Weet u zeker dat u de selectie wilt afwijzen?<br>' +
            'Na afwijzing wordt de status veranderd naar \'Datauitgifte.\'',
        'Are you sure you want to submit the selection?<br>You may enter a remark:':
            'Weet u zeker dat u de selectie wilt insturen?<br>U kunt hier een opmerking plaatsen:',
        'Remark': 'Opmerking',

        'Authentification checks by PALGA': 'Authentificatiecontroles door PALGA',
        'Requester\'s identity was established.': 'De identiteit van de aanvrager is vastgesteld/gecontroleerd.',
        'Requester indeed works at or collaborates with the pathology laboratory mentioned.': 'De aanvrager is inderdaad verbonden aan of werkt samen met het opgegeven pathologielaboratorium.',
        'The requester is allowed to submit the request.': 'De aanvrager is bevoegd deze aanvraag in te dienen.',
        'The contact person been checked and is authorised to support the request.': 'De hoofdaanvrager is bekend en ondersteunt deze aanvraag.',
        'Authentification and agreement request': 'Authentificatie en instemming aanvraag',
        'Agreement has been reached.': 'Formulier ontvangen',
        'Upload form': 'Upload formulier',
        'Upload signed authentificaton and agreement form': 'Upload ondertekend formulier authentificatie en instemming aanvraag',
        'No form uploaded.': 'Nog geen formulier geüpload.',
        'Reject': 'Afwijzen',
        'Submit to scientific council': 'Doorsturen naar wetenschappelijke raad',

        'No previous contact.': 'Aanvrager heeft niet eerder contact gehad met betrekking tot deze aanvraag',
        'Data linkage': 'Koppeling',
        'Linkage with own patients or cohort or linkage between registries.': 'Koppeling van eigen patiënten of eigen cohort of koppeling tussen registraties.',
        'No data linkage required.': 'Geen koppeling vereist.',
        'Informed consent (IC)': 'Informed consent (IC)',
        'popover: Informed consent': 'Informed consent (IC).',
        'Data linkage information': 'Koppelingsinformatie',
        'popover: Data linkage': 'Koppelingsinformatie.',
        'Which cohort or registry do you want to link to PALGA?': 'Geef aan met welke persoonsgegevens of welke registratie u wilt koppelen.',
        'popover: Notes for linkage with personal data':  'Bijvoorbeeld naam en website van uw eigen cohort of naam van de registratie (NKR, DICA, PHARMO, etc.).',
        'Explanation why linkage is allowed without informed consent': 'Toelichting gebruik persoonsgegevens zonder toestemming (informed consent)',
        'popover: Reason using personal data': 'Geef aan waarom u denkt dat informed consent niet nodig is.',
        'Notes': 'Notities',
        'Add note': 'Notitie toevoegen',
        'Edit request': 'Aanvraag bewerken',
        'Pathologist email': 'E-mail patholoog',
        'General details': 'Algemene details',
        'Review by PALGA privacy commitee': 'Toetsing door PALGA privacycommissie (PPC)',
        'This request was sent by email to the privacy committee.': 'Deze aanvraag is schriftelijk voorgelegd naar de PPC',
        'Review PPC': 'Beoordeling PPC',
        'Review result': 'Oordeel PPC',
        'Explanation for PPC': 'Toelichting voor PPC',
        'Summary review procces': 'Samenvatting schriftelijke procedure',
        'Final checks before data delivery': 'Laatste controles voor datalevering',
        'Approved by scientific council.': 'De wetenschappelijke raad heeft toestemming gegeven',
        'Approved by privacy committee.': 'Afgehandeld conform mandaat PPC',
        'Finish': 'Afronden',
        'Contact information': 'Contactinformatie',
        'Validation': 'Validatie',
        'Reopen': 'Heropenen',
        'Print selected': 'Selectie afdrukken',

        'All requests': 'Alle aanvragen',
        'Requests claimed by me': 'Door mij geclaimde aanvragen',
        'Unclaimed requests': 'Openstaande aanvragen',
        'Requests on hold': 'Aanvragen in de wacht',
        'Reviewed': 'Beoordeeld',
        'Not yet reviewed': 'Nog niet beoordeeld',

        'All lab requests': 'Alle labverzoeken',
        'Lab requests claimed by me': 'Door mij geclaimde labverzoeken',
        'Unclaimed lab requests': 'Openstaande labverzoeken',

        'Review by the scientific council': 'Beoordeling door de wetenschappelijke raad',
        'Review_short': 'Beoordeling',
        'Discussion': 'Discussie',
        'Update comment': 'Opmerking bewerken',
        'Add comment': 'Opmerking plaatsen',
        'popover: Upload file': 'Upload alle relevante bijlagen.',
        'popover: Upload medical ethical committee approval': 'Upload brief van medisch-ethische toetsingscommissie.',
        'Close request': 'Aanvraag afsluiten',
        'Compose mail to members': 'Schrijf e-mail aan leden',
        'Compose reopen email': 'Meld heropenen aanvraag',

        'ppc_handled_according_mandate': 'Afgehandeld conform mandaat PPC',
        'ppc_handled_according_mandate_explanation': 'Afgehandeld conform mandaat PPC, zie toelichting',
        'ppc_approved_written_procedure': 'Goedgekeurd na schriftelijke procedure',
        'ppc_discuss': 'Bespreken in vergadering',
        'ppc_rationale_exploratory_request': 'Oriënterende aanvraag, geen oordeel vereist',

        'Excerpt list': 'Excerptlijst',
        'Upload excerpt list': 'Excerptlijst uploaden',
        'Select all excerpts and continue': 'Alle excerpten selecteren en doorgaan',
        'Excerpt selection': 'Excerptselectie',
        'Download selection': 'Selectie downloaden',

        'Other data files': 'Overige databestanden',
        'No data files.': 'Geen databestanden.',
        'Upload data files': 'Databestanden uploaden',

        /* ========= */
        /* Login */
        /* ========= */
    
        // Username: 'Gebruikersnaam',
        Login: 'Aanmelden',
        login: 'aanmelden',
        Logout: 'Afmelden',
        'Forgot password': 'Wachtwoord vergeten',
        'Create an account': 'Een account maken',
        'Bad credentials': 'Ongeldige logingegevens.',
        'User account blocked. Please retry in 15 minutes.': 'Account geblokkeerd. Probeert u het opnieuw over 15 minuten.',
        'Email address (lower case)': 'E-mailadres (kleine letters)',

        /* ========= */
        /* Main page */
        /* ========= */
    
        Language: 'Taal',
        English: 'Engels',
        Dutch: 'Nederlands',
        Task: 'Taak',
        Tasks: 'Taken',
        Registration: 'Registeren',
        Request: 'Aanvraag',
        Requests: 'Aanvragen',
        'Not logged in': 'Niet ingelogd',
        Description: 'Omschrijving',
        'Attachments': 'Bijlagen',
        'done': 'gedaan',
        'Completed requests': 'Afgeronde aanvragen',
        'New request': 'Nieuwe aanvraag',
        'Access is denied': 'Toegang is geweigerd',

        /* ================= */
        /* Registration form */
        /* ================= */

        'First name': 'Voornaam',
        'Last name': 'Achternaam',
        'Telephone number': 'Telefoonnummer',
        'Institute': 'Instelling',
        'Specialism': 'Specialisme',
        'Department': 'Afdeling',
        'I am member of a pathology department': 'Ik ben lid van een pathologieafdeling',
        'Password': 'Wachtwoord',
        'Repeat password': 'Herhaal wachtwoord',
        'Request account': 'Account aanvragen',
        'Required field': 'Verplicht veld',
        'Input value is too long': 'Invoerwaarde is te lang',
        'Pathology lab': 'Pathologielaboratorium',
        'Select lab': 'Selecteer laboratorium',
    
        /* ================= */
        /* Email validation */
        /* ================= */
    
        'Account activation': 'Account activeren',
        'Your account has been successfully activated.': 'Uw account is succesvol geactiveerd.',
        'This link is not valid.': 'Deze link is niet geldig.',
    
        /* ================= */
        /* Profile */
        /* ================= */
    
        'Profile': 'Profiel',
        'Update profile': 'Profiel bijwerken',
        'Change password': 'Wachtwoord wijzigen',
        'Update': 'Bijwerken',
        'You are only allowed to edit your telephone number. If you would like to change other information please contact PALGA.': 
            'U kunt alleen uw telefoonnummer aanpassen. Indien u andere informatie wilt wijzigen, neem dan contact op met PALGA.',
        'Your profile has been updated': 'Uw profiel is bijgewerkt',
    
        /* ===================== */
        /* Change/reset password */
        /* ===================== */
    
        'Old password': 'Oud wachtwoord',
        'New password': 'Nieuw wachtwoord',
        'Repeat new password': 'Herhaal nieuw wachtwoord',
        'The old password is incorrect': 'Het oude wachtwoord is niet correct',
        'Change': 'Wijzigen',
        'Reset': 'Resetten',
        'Reset password': 'Wachtwoord resetten',
        'The password reset link is invalid.': 'De link om het wachtwoord te resetten is niet geldig.',
        'Receive a new link.': 'Ontvang een nieuwe link.',
        'Your password has been successfully reset.': 'Uw wachtwoord is successvol gereset.',
        'Send email': 'Stuur e-mail',
        'An email has been sent to ? with instructions on how to reset your password.': 'We hebben een email gestuurd naar {{email}} met instructies om uw wachtwoord te resetten.',
        'Do not forget to store the password in a secure location. It is advised to use the password manager of your browser.': 'Vergeet niet het wachtwoord op een veilige locatie te bewaren. Het wordt aangeraden om de password manager van uw browser te gebruiken.',
        'Generate password': 'Genereer een wachtwoord',
    
        /* ===================== */
        /* Request Overview */
        /* ===================== */
        'Overview': 'Overzicht',
        'Create new request': 'Nieuwe aanvraag',
        'Title': 'Titel',
        'Status': 'Status',
        'Creation date': 'Aangemaakt op',
        'Requester': 'Aanvrager',
        'Assignee': 'Gevolmachtigde',
        'PALGA advisor': 'PALGA-adviseur',
        'Actions': 'Acties',
        'Details': 'Details',
        'Claim': 'Claim',
        'Unclaim': 'Vrijgeven',
        'Edit': 'Bewerk',
        'Delete': 'Verwijder',
        'Filter by title': 'Filteren op titel',
        'Filter by status': 'Filteren op status',
        'Filter by requester': 'Filteren op aanvrager',
        'Filter by assignee': 'Filteren op gevolmachtigde',
        'Filter by PALGA advisor': 'Filteren op PALGA-adviseur',
        'Date assigned': 'Toegekend op',
        'My verdict': 'Mijn oordeel',

        /* ===================== */
        /* Lab request overview */
        /* ===================== */
        'Request title': 'Titel',
        'Lab request ID': 'Labverzoek ID',
        'End date': 'Einddatum',
        '# PA numbers': '# PA-nummers',
        'Filter by id': 'Filteren op ID',
        'Filter by request title': 'Filteren op titel',
        'Filter by lab': 'Filteren op lab',

        /* ===================== */
        /* Lab request details */
        /* ===================== */
        'Approve': 'Goedkeuren',
        'Request details': 'Aanvraagdetails',
        'Request ID': 'Aanvraag ID',
        'Requester lab': 'Lab aanvrager',
        'Sending lab': 'Versturend lab',
        'Lab request code': 'Labverzoekcode',
        'Are you sure you want to reject the lab request?': 'Weet u zeker dat u het labverzoek wilt afwijzen?',
        'Approve this lab request?': 'Het labverzoek goedkeuren?',
        'Is the material being sent?': 'Is het materiaal verstuurd?',
        'Has the material been received?': 'Is het materiaal ontvangen?',
        'Return the material to the sending lab?': 'Het materiaal terugsturen naar het versturende lab?',
        'Has the material been received in return?': 'Is het materiaal terugontvangen?',
        'Complete PA reports request?': 'Het verzoek op PA-verlagen afronden?',
        'Complete rejected request?': 'Het afgewezen labverzoek afronden?',
        'Are you sure you want to delete the PA number?': 'Weet u zeker dat u het PA-nummer wilt verwijderen?',
        'Return this lab request to status \'Under review by lab\'?':
            'Het labverzoek terugzetten naar \'Beoordeling door lab\'?',
        'Undo rejection': 'Afwijzing ongedaan maken',
        'A researcher has requested data and/or materials from your lab. Do you approve this request?':
            'Een onderzoeker heeft gegevens en/of materiaal gevraagd aan uw lab. Stemt u in met dit verzoek?',
        'Complete': 'Afronden',
        'Available' : 'Beschikbaar',
        'Not available' : 'Niet beschikbaar',
        'Samples are not available': 'Samples zijn niet beschikbaar',
        'Sequence number': 'Volgnummer',
        'Enter sample codes': 'Vul samplecodes in',
        'Number of samples': 'Aantal samples',
        'PA reports have been sent to the requester.': 'PA-verslagen zijn naar de aanvrager verstuurd.',
        'PA reports have NOT been sent to the requester.': 'PA-verslagen zijn NIET naar de aanvrager verstuurd.',
        'Update PA reports status': 'Verzendstatus PA-verslagen opslaan',
        'Clinical data has been sent to the requester.': 'Klinische gegevens zijn naar de aanvrager verstuurd.',
        'Clinical data has NOT been sent to the requester.': 'Klinische gegevens zijn NIET naar de aanvrager verstuurd.',
        'Update clinical data status': 'Verzendstatus klinische data opslaan',
        'The lab request has been approved.': 'Het labverzoek is goedgekeurd.',

        /* ===================== */
        /* User overview */
        /* ===================== */
        'Add user': 'Gebruiker toevoegen',
        'Name': 'Naam',
        'Email': 'E-mail',
        'Email address': 'E-mailadres',
        'Role': 'Rol',
        'Created': 'Aangemaakt op',
        'Activate': 'Activeer',
        'Deactivate': 'Blokkeer',

        /* ===================== */
        /* Add user */
        /* ===================== */
        'Edit user': 'Bewerk gebruiker',
        'Edit user ...': 'Bewerk gebruiker {{username}}',
        'Please complete all required fields.': 'Alle velden invullen a.u.b.',
        'Lab': 'Laboratorium',
        'None': 'Geen',

        /* ===================== */
        /* Sample overview */
        /* ===================== */

        'PA number': 'PA-nummer',
        'PA numbers': 'PA-nummers',
        'Samples': 'Samples',
        'Lab request': 'Labverzoek',
        'Filter by samples': 'Filteren op monsters',
        'Filter by PA number': 'Filteren op PA-nummer',

        /* ===================== */
        /* Lab overview */
        /* ===================== */
        'Add lab': 'Lab toevoegen',
        'Edit lab': 'Lab bewerken',
        'Number': 'Labnummer',

        /* ===================== */
        /* Scientific council */
        /* ===================== */
        'Accepted': 'Akkoord',
        'Rejected': 'Afgewezen',
        'ACCEPTED': 'Akkoord',
        'REJECTED': 'Afgewezen',

        /* ===================== */
        /* Status */
        /* ===================== */
        'Open': 'Open, nog niet ingestuurd',
        'Review': 'Ontvangen door PALGA-adviseur',
        'Approval': 'Wachten op goedkeuring',
        'DataDelivery': 'Datalevering en -selectie',
        'SelectionReview': 'Excerptselectie ingestuurd',
        'LabRequest': 'Verzoek aan labs',
        'Waiting for lab approval': 'Beoordeling door lab',
        'Approved': 'Goedgekeurd',
        'Sending': 'Materiaal verstuurd',
        'Received': 'Ontvangen',
        'Returning': 'Retour verzonden',
        'Returned': 'Retour ontvangen',
        'Completed': 'Afgerond',
        'Closed': 'Afgesloten',

        'Download ...': '{{filename}} downloaden',
        'Uploading ?' : 'Bezig met uploaden van {{filename}}',
        'Done uploading file ?' : 'Klaar met uploaden van {{filename}}',


        /* ===================== */
        /* Agreement overview */
        /* ===================== */
        'popover:Markdown Help': 'Markdown Hulp',
        'Edit form': 'Formulier aanpassen',
        'Insert variables': 'Variabelen invoegen',

        /* ===================== */
        /* My Lab */
        /* ===================== */
        'My lab': 'Mijn lab',
        'Edit my lab': 'Bewerk mijn lab',
        'Enable hub assistance.': 'Inzet hubmedewerker toestaan.',
        'Would you like hub assistance for this lab request?': 'Wilt u de inzet van de hubmedewerker toestaan voor dit labverzoek?',
        'Request hub assistance.': 'Hubmedewerker inschakelen.',
        'Option not available because hub assistance is not enabled by your lab.': 'De optie is niet beschikbaar omdat uw lab de inzet van de hubmedewerker niet toestaat.',
        'Active': 'Actief',
        'Inactive': 'Niet actief',
        'Lab Id' :'Lab Id',
        'Hub assistance' :'Inzet hubmedewerker',
        'Hub users': 'Hubmedewerkers',
        'Email addresses': 'Emailadressen',
        'Update hub assistance': 'Inzet hubmedewerker aanpassen',
        'Actions in status': 'Acties in status',

        /* ===================== */
        /* Specialism */
        /* ===================== */
        'Maag-darm-lever-ziekten':'Maag-darm-lever-ziekten',
        'Gynaecologie':'Gynaecologie',
        'Dermatologie':'Dermatologie',
        'Medische Oncologie':'Medische Oncologie',
        'Interne geneeskunde':'Interne geneeskunde',
        'Radiologie':'Radiologie',
        'Radiotherapie':'Radiotherapie',
        'Chirurgie':'Chirurgie',
        'Hematologie':'Hematologie',
        'Keel-neus-oor':'Keel-neus-oor',
        'Heelkunde':'Heelkunde',
        'Epidemiologie':'Epidemiologie',
        'Eerstelijnsgeneeskunde':'Eerstelijnsgeneeskunde',
        'Cardiologie':'Cardiologie',
        'Pathologie':'Pathologie',
        'Longziekten':'Longziekten',
        'Urologie':'Urologie',
        'Neurologie':'Neurologie',
        'Endocrinologie':'Endocrinologie',
        '(Other)':'(Overig)',
        '(Please select a specialism)' : '(Kies een specialisme)',

        null: null
    };

    if (hasWindow) {
        var messages = window.messages || (window.messages = {});
        messages.nl = _messages_nl;
    }

    if (hasModuleExports) {
        module.exports = _messages_nl;
    }

})(typeof module !== 'undefined' && module.exports, typeof window !== 'undefined');
