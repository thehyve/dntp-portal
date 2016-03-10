/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(window) {
    'use strict';
    
    /* global messages: true,
     */

    var messages = window.messages || (window.messages = {});
    
    messages.nl = {
        /* ========= */
        /* Global */
        /* ========= */

        'Source': 'Bron',
        'DNTP': 'PALGA',
        'The password has been successfully changed': 'Het wachtwoord is succesvol gewijzigd',
        'There was a problem logging in.': 'Er is een probleem met inloggen.',
        'Passwords must be at least 8 characters long': 'Wachtwoorden moeten minstens 8 tekens lang zijn',
        'No lab associated with lab user.': 'Deze gebruiker is niet verbonden aan een lab.',
        'Search': 'Zoeken',
        'Lab requests': 'Labverzoeken',
        'About': 'Over',
        'Username': 'E-mailadres',
        'Users': 'Gebruikersbeheer',
        'Labs': 'Laboratoriumbeheer',
        'Passwords do not match': 'De wachtwoorden zijn niet gelijk',
        'Thank you': 'Bedankt',
        'Registration has been completed. An email with an activation link should have been sent to ?.': 'Registratie is afgerond. Een e-mail met de activatielink is gestuurd naar {{email}}.',
        'Password must be 8 or more characters long, contain numbers, letters, and a special character.': 'Wachtwoord moet ten minste 8 tekens lang zijn en cijfers, letters en een speciaal teken bevatten.',

        /* ========= */
        /* Request */
        /* ========= */
        'Principal investigator': 'Hoofdaanvrager',
        'popover: Principal investigator': 'Hoofdaanvrager is de eindverantwoordelijk onderzoeker.',
        'popover: Pathologist': 'De patholoog betrokken bij dit onderzoek. ',
        'Pathologist': 'Patholoog',
        'popover: Pathologist email': 'Vul het e-mail adres van de betrokken patholoog in.',
        'Postal code': 'Postcode',
        'popover: Postal code': 'De postcode.',
        'City': 'Plaats',
        'popover: City': 'De plaats.',
        'Address': 'Adres',
        'popover: Address': 'Postadres waar factuur naar verzonden mag worden. ',
        'Province': 'Provincie',
        'popover: Province': 'De provincie.',
        'popover: E-mail': 'E-mail crediteurenadministratie.',
        'popover: Telephone': 'Het telefoonnummer.',
        'Billing address': 'Facturatieadres',
        'Billing information': 'Facturatiedetails',
        'Charge number': 'Kostenplaats',
        'popover: Charge number': 'Rekening waar het project op geboekt mag worden.',
        'Research number': 'Onderzoeksnummer',
        'popover: Research number': 'Projectnummer van bijvoorbeeld subsidieverstrekker.',
        'Previous contact': 'Vorig contact',
        'popover: Previous contact': 'Heeft u contact gehad met PALGA over deze aanvraag?',
        'Did you contact Palga previously?': 'Heeft u contact gehad met PALGA over deze aanvraag?',
        'popover: Description of previous contact': 'Met wie heeft u contact gehad en wat is besproken?',
        'Description of previous contact': 'Beschrijf het contact.',
        'Yes': 'Ja',
        'No': 'Nee',
        'Running title': 'Onderzoekstitel',
        'popover: Title': 'De titel van het onderzoek.',
        'Background': 'Achtergrond',
        'popover: Background': 'Achtergrond van het onderzoek.',
        'Research question': 'Onderzoeksvraag',
        'popover: Research question': 'De onderzoeksvraag.',
        'Hypothesis': 'Hypothese',
        'popover: Hypothesis': 'De hypothese.',
        'Methods': 'Methode',
        'popover: Methods': 'Welke technieken(bv Kiembaan mutatie analyse /moleculaire technieken,immuunhistochemie, tissue micro array enz.) worden gebruikt om tot beantwoording van de hypothese te komen? wat is het minimaal aantal patienten dat nodig is om deze vraag te kunnen beantwoorden?',
        'Request type': 'Soort aanvraag',
        'popover: Request type': 'Een excerpt is een samenvatting van het oorspronkelijke pathologie verslag en bevat onder meer de diagnoseregels en de conclusie van de patholoog. Zie voor verdere uitleg palga.nl.',
    
    
    
        'Request for numbers only': 'Getallen PALGA-database',
        'Request for excerpts': 'Excerpten',
        'Request for excerpts + PA reports': 'Excerpten en complete PA-verslagen',
        'Request for excerpts + materials': 'Excerpten en materiaal',
        'Request for excerpts + PA reports + materials': 'Excerpten, complete PA-verslagen en materiaal',
        'Request for PA-reports only': 'Alleen PA-verslagen',
        'Request for materials only': 'Alleen materiaal',
    
        'Request_Type_1': 'Getallen PALGA-database',
        'Request_Type_2': 'Excerpten',
        'Request_Type_3': 'Excerpten en complete PA-verslagen',
        'Request_Type_4': 'Excerpten en materiaal',
        'Request_Type_5': 'Excerpten, complete PA-verslagen en materiaal',
        'Request_Type_6': 'Alleen PA-verslagen',
        'Request_Type_7': 'Alleen materiaal',
    
        'Upload file': 'Upload bestand(en)',
        'drag and drop your file here': 'sleep bestanden hierheen',
        'No attachment found.': 'Nog geen attachments geüpload.',
        'Medical ethical committee approval': 'METC toetsing',
        'Upload medical ethical committee approval': 'Upload METC-toetsing',
        'Submit': 'Indienen',
        'Save': 'Opslaan',
        'Cancel': 'Annuleren',
        'Are you sure you want to delete file ?': 'Weet u zeker dat u het bestand {{name}} wilt verwijderen?',
        'File too large': 'Het bestand is te groot ({{mb_size}} MB). Het maximum is 10 MB.',
        'Are you sure you want to approve the selection?<br>After approving, lab requests will be generated.' :
            'Weet u zeker dat u de selectie wilt goedkeuren?<br>Na goedkeuring worden labverzoeken aangemaakt.',
        'Are you sure you want to delete request ?': 'Weet u zeker dat u aanvraag {{id}} wilt verwijderen?',
        'Are you sure you want to submit the request? After submission the request cannot be edited anymore.' :
            'Weet u zeker dat u de aanvraag wilt indienen? Na het indienen kan de aanvraag niet meer bewerkt worden.',
        'Are you sure you want to submit the request for approval?': 'Weet u zeker dat u de aanvraag voor goedkeuring wilt doorsturen?',
        'Are you sure you want to finalise the request?': 'Weet u zeker dat u de aanvraag wilt afronden?',
        'Are you sure you want to close the request?<br>After closing, no data files can be added.': 'Weet u zeker dat u de aanvraag wilt sluiten?<br>Na het sluiten kunnen geen bestanden worden toegevoegd.',
        'Are you sure you want to reject the request?': 'Weet u zeker dat u de aanvraag wilt afwijzen?',
        'Please enter a reject reason:': 'Reden van afwijzing:',
        'Are you sure you want to reject the selection?<br>After rejecting, the status will return to \'Data delivery.\'' :
            'Weet u zeker dat u de selectie wilt afwijzen?<br>' +
            'Na afwijzing wordt de status veranderd naar \'Datauitgifte.\'',
    
        'Checks by Palga': 'Controles door PALGA uitgevoerd',
        'The requester has been checked and is valid.': 'De identiteit van de aanvrager is vastgesteld/gecontrolleerd.',
        'The requester has been confirmed to be associated with the lab.': 'De aanvrager is verbonden aan het opgegeven laboratorium.',
        'The requester is allowed to file the request.': 'De aanvrager is gemachtigd deze aanvraag in te dienen.',
        'The contact person been checked and is authorised to support the request.': 'De hoofdaanvrager is bekend en ondersteunt deze aanvraag.',
        'Signed agreement': 'Handtekeningformulier',
        'Agreement has been reached.': 'Formulier ontvangen',
        'Upload agreement files': 'Upload handtekeningenformulier',
        'Reject': 'Afwijzen',
        'Submit for approval': 'Doorsturen naar wetenschappelijke raad',
    
        'No previous contact.': 'Aanvrager heeft niet eerder contact gehad met betrekking tot deze aanvraag',
        'Data linkage': 'Koppeling',
        'Linkage with own or external personal data.': 'Koppeling met eigen of externe data.',
        'No data linkage required.': 'Geen koppelingen met eigen of externe data.',
        'Informed consent (IC)': 'Informed consent (IC)',
        'Data linkage notes': 'Koppelingsinformatie',
        'popover: Data linkage': 'Koppelingsinformatie',
        'Notes for linkage with personal data':  'Geef aan met welk bestand of welke organisatie u wilt koppelen.',
        'popover: Notes for linkage with personal data':  'Bijvoorbeeld uw eigen cohort, IKNL, DICA etc. ',
        'Reason using personal data': 'Reden ontbreken IC',
        'popover: Reason using personal data': 'Geef aan waarom u denk dat informed consent niet nodig is.',
        'Notes': 'Notities',
        'Add note': 'Notitie toevoegen',
        'Edit request': 'Aanvraag bewerken',
        'Pathologist email': 'E-mail patholoog',
        'General details': 'Algemene details',
    
        'Votes': 'Stemmen',
        'Approval by the scientific council': 'Beoordeling door de wetenschappelijke raad',
        'Discussion': 'Discussie',
        'Update comment': 'Opmerking bewerken',
        'Add comment': 'Opmerking plaatsen',
        'popover: Upload file': 'Upload alle relevante bijlagen. ',
        'popover: Upload medical ethical committee approval': 'Upload verklaring METC indien van toepassing.',
    
    
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
        'Telephone': 'Telefoonnummer',
        'Institute': 'Instelling',
        'Specialism': 'Specialisme',
        'Department': 'Afdeling',
        'I am member of a pathology department': 'Ik ben lid van een pathologieafdeling',
        'Password': 'Wachtwoord',
        'Repeat password': 'Herhaal wachtwoord',
        'Request account': 'Account aanvragen',
        'Required field': 'Veld is verplicht',
        'Input value is too long': 'Invoerwaarde is te lang',
        'Pathology lab': 'Pathologielaboratorium',
    
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
        'You are only allowed to edit your telephone number. If you would like to change other information please contact PALGA': 'Het bent alleen toegestaan om uw telefoonnummer aan te passen. Als u ook andere informatie wil bijwerken, neem contact op met PALGA',
        'Your profile has been updated': 'Uw profiel is bijgewerkt',
    
        /* ===================== */
        /* Change/reset password */
        /* ===================== */
    
        'Old password': 'Oud wachtwoord',
        'New password': 'Nieuw wachtwoord',
        'New password (again)': 'Nieuw wachtwoord (herhaling)',
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
        'Date assigned': 'Toegekend op',
        'Vote': 'Stem(men)',
    
        /* ===================== */
        /* Labrequest overview */
        /* ===================== */
        'Request title': 'Titel',
        'Lab request ID': 'Labverzoek ID',
        'End date': 'Einddatum',
        '# PA numbers': '# PA-nummers',
        'Filter by id': 'Filteren op ID',
        'Filter by request title': 'Filteren op titel',
        'Filter by lab': 'Filteren op lab',
    
    
        /* ===================== */
        /* Labrequest details */
        /* ===================== */
        'Request details': 'Aanvraagdetails',
        'Request ID': 'Aanvraag ID',
        'Requester lab': 'Lab aanvrager',
        'Sender lab': 'Versturend lab',
        'Lab request code': 'Labverzoekcode',
    
    
    
        /* ===================== */
        /* User Overview */
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
        /* Sample overview */
        /* ===================== */
        'Add lab': 'Lab toevoegen',
        'Edit lab': 'Lab bewerken',
        'Number': 'Labnummer',
    
        /* ===================== */
        /* scientific council */
        /* ===================== */
        'Accepted': 'Instemmen',
        'Rejected': 'Afgewezen',
        'ACCEPTED': 'Akkoord',
        'REJECTED': 'Afgewezen',
    
        /* ===================== */
        /* status */
        /* ===================== */
        'Open': 'Niet ingestuurd',
        'Review': 'In behandeling',
        'Approval': 'Wachten op goedkeuring',
        'DataDelivery': 'Datalevering en -selectie',
        'SelectionReview': 'Wachten op goedkeuring excerptselectie',
        'LabRequest': 'Verzoek aan labs',
        'Waiting for lab approval': 'In afwachting beoordeling lab',
        'Approved': 'Goedgekeurd',
        'Sending': 'Materiaal verstuurd',
        'Received': 'Ontvangen',
        'Returning': 'Retour verzonden',
        'Returned': 'Retour ontvangen',
        'Completed': 'Afgerond',
        'Closed': 'Afgesloten',
    
        'Download ...': '{{filename}} downloaden',
    
    
        /* ===================== */
        /* Agreement overview */
        /* ===================== */
        'popover:Markdown Help': 'Markdown Hulp',
        'Agreement form template': 'Agreement form template',
    
        null: null
    };
})(window);
