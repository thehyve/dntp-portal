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
        'role_palga': 'PALGA',
        'role_requester': 'Aanvrager',
        'role_lab_user': 'Labmedewerker',
        'role_hub_user': 'Hubmedewerker',
        'role_scientific_council': 'Wetenschappelijke raad',

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
        'User has been added. A password reset mail has been sent to ?.':
            'Gebruiker is toegevoegd. De gebruiker ontvangt een mail op {{email}} om het wachtwoord aan te passen.',
        'Are you sure you want to delete user ? ?': 'Weet je zeker dat je gebruiker {{name}} wilt verwijderen?',
        'only visible for PALGA': 'alleen zichtbaar voor PALGA-adviseurs',
        'Export requests': 'Aanvragen exporteren',
        'Main request': 'Hoofdaanvraag',
        'User disabled': 'Gebruiker geblokkeerd',
        'Laboratory disabled': 'Laboratorium geblokkeerd',
        'Remove': 'Verwijder',
        'Suspend': 'Pauzeren',
        'Resume': 'Hervatten',

        /* ========= */
        /* Request */
        /* ========= */
        'Principal investigator': 'Projectleider',
        'popover: Principal investigator': 'Projectleider is de eindverantwoordelijk onderzoeker.',
        'Principal investigator email': 'E-mail projectleider',
        'popover: Pathologist': 'De patholoog betrokken bij dit onderzoek. ',
        'Pathologist': 'Patholoog',
        'Postal code': 'Postcode',
        'popover: Postal code': 'De postcode.',
        'City': 'Plaats',
        'popover: City': 'De plaats.',
        'Address': 'Adres',
        'popover: Address': 'Postadres waar factuur naar verzonden mag worden. ',
        'popover: E-mail': 'E-mail crediteurenadministratie.',
        'Billing address': 'Facturatieadres',
        'Delivery address': 'Afleveradres',
        'Billing details': 'Facturatiedetails',
        'Charge number': 'Kostenplaats',
        'popover: Charge number': 'Kostenplaats van het project.',
        'Grant provider': 'Subsidieverstrekker',
        'popover: Grant provider': 'Subsidieverstrekker van het project.',
        'Grant number': 'Subsidienummer',
        'popover: Research number': 'Projectnummer van subsidieverstrekker.',
        'Previous contact': 'Vorig contact',
        'Did you contact Palga previously?': 'Heeft u contact gehad met PALGA over deze aanvraag?',
        'popover: Description of previous contact': 'Met wie heeft u contact gehad en wat is besproken?',
        'Description of previous contact.': 'Beschrijf het contact.',
        'Yes': 'Ja',
        'No': 'Nee',
        'Running title': 'Titel onderzoeksproject',
        'popover: Title': 'De titel van het onderzoek (max. 100 tekens).',
        'Background': 'Achtergrond',
        'popover: Background': 'Achtergrond van het onderzoek.',
        'Research question': 'Onderzoeksvraag',
        'popover: Research question': 'De onderzoeksvraag of -vragen.',
        'Hypothesis': 'Hypothese',
        'popover: Hypothesis': 'De hypothese.',
        'Methods': 'Methode',
        'popover: Methods': 'De onderzoeksmethode die u gaat gebruiken zoals trial, prospectief/retrospectief cohort, patiëntcontrole onderzoek etc, de uitkomstmaat en beoogde data-analyses. Wat is het minimaal aantal patiënten dat nodig is om deze vraag te kunnen beantwoorden?',
        'Search criteria': 'Zoekcriteria',
        'popover: Search criteria': 'Mocht u al een idee hebben voor de gewenste zoekcriteria, zoals soort onderzoek of specifieke PALGA-termen, dan kunt u die hier noteren. U kunt ook de algemene medische termen noteren, dan overleggen wij later met u over de exacte zoekcriteria.',
        'popover: Biobank request': 'Vraag na bij uw Biobank / Pathologie afdeling of er een Biobank aanvraagnummer vereist is',
        'Study period': 'Studieperiode',
        'Laboratory techniques': 'Laboratoriumtechnieken',
        'popover: Laboratory techniques': 'Indien van toepassing: welke labtechnieken (bijv. kiembaanmutatieanalyse/moleculaire technieken, immuunhistochemie, tissue micro array etc.) worden gebruikt?',
        'Request type': 'Benodigde data en/of materialen',
        'popover: Request type': 'Benodigde data en/of materialen. Voor uitleg over PALGA-gegevens, bijv. welke gegevens een excerpt bevat, zie <a target="_blank" tabindex="-1" href="http://www.palga.nl/gegevensaanvragen/zoekvragen.html">www.palga.nl/gegevensaanvragen/zoekvragen.html</a>.',

        'Numbers only, exploratory request': 'Alleen aantallen; oriënterende zoekvraag',
        'Excerpts only': 'Alleen excerpten',
        'Excerpts and full PA reports': 'Excerpten en complete PA-verslagen',
        'Excerpts and PA material': 'Excerpten en PA-materiaal',
        'Excerpts, PA reports and PA material': 'Excerpten, complete PA-verslagen en PA-materiaal',
        'Full PA reports only (no excerpts)': 'Alleen PA-verslagen',
        'PA material only': 'Alleen PA-materiaal',
        'Excerpts, reports, material and/or clinical data': 'Excerpten, PA-verslagen, PA-materiaal en/of klinische gegevens',
        'Excerpts': 'Excerpten',
        'PA reports': 'Complete PA-verslagen',
        'PA material': 'PA-materiaal',
        'Clinical data': 'Klinische gegevens',
        'Clinical data from practitioner': 'Klinische gegevens via behandelaar',
        'Biobank request number': 'Biobank aanvraagnummer',
        'Hereby you receive this material for a scientific study. We request you notify': 'Hierbij ontvangt u materiaal voor een wetenschappelijke studie. Wij verzoeken u dit door te geven aan',
        'PALGA request': 'PALGA aanvraag',
        'Request_Type_1': 'Alleen aantallen, oriënterende zoekvraag',
        'Request_Type_2': 'Alleen excerpten',
        'Request_Type_3': 'Excerpten en complete PA-verslagen',
        'Request_Type_4': 'Excerpten en PA-materiaal',
        'Request_Type_5': 'Excerpten, complete PA-verslagen en PA-materiaal',
        'Request_Type_6': 'Alleen PA-verslagen',
        'Request_Type_7': 'Alleen PA-materiaal',
        'Germline mutation analysis': 'Kiembaanmutatieanalyse',
        'Additional request for request ?': 'Aanvullend verzoek bij {{requestNumber}}',
        'Upload file': 'Upload bestand(en)',
        'drag and drop your file here': 'sleep bestanden hierheen',
        'No attachments uploaded.': 'Nog geen attachments geüpload.',
        'No forms uploaded.': 'Nog geen formulieren geüpload.',
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
        'Are you sure you want to finalise the request?': 'Weet je zeker dat je de aanvraagprocedure wilt afronden?',
        'Are you sure you want to close the request?<br>After closing, no data files can be added.': 'Weet je zeker dat je de aanvraag wilt afsluiten?<br>Na het sluiten kunnen geen bestanden meer worden toegevoegd.',
        'Are you sure you want to reject the request?': 'Weet je zeker dat je de aanvraag wilt afwijzen?',
        'Please enter the reason for rejection.': 'Vul de reden van afwijzing in.',
        'Please enter the reason for rejection (max. 2000 characters).': 'Vul de reden van afwijzing in (max. 2000 tekens).', 
        'Are you sure you want to reject the selection?<br>After rejecting, the status will return to \'Approved, waiting for data.\'' :
            'Weet je zeker dat je de selectie wilt afwijzen?<br>' +
            'Na afwijzing wordt de status veranderd naar \'Goedgekeurd, wachten op data.\'',
        'Are you sure you want to submit the selection?': 'Weet u zeker dat u de selectie wilt insturen?',
        'Remark': 'Opmerking',
        'The excerpt list exceeds the limit of ?. The requester cannot use the excerpt selection interface for this request.':
            'De excerptlijst overschrijft de grens van {{limit}} excerpten. De aanvrager kan daardoor de interface voor excerptselectie niet gebruiken.',
        'Upload excerpt selection': 'Excerptselectie uploaden',
        'Submit selection': 'Selectie opsturen',
        'Select PA numbers': 'PA-nummers selecteren',
        'Manual selection disabled for large excerpt lists.': 'De interface voor excerptselectie is uitgeschakeld voor grote excerptlijsten.',
        'No excerpt list.': 'Geen excerptlijst.',

        'Authentification checks by PALGA': 'Authentificatiecontroles door PALGA',
        'Requester\'s identity was established.': 'De identiteit van de aanvrager is vastgesteld/gecontroleerd.',
        'Requester indeed works at or collaborates with the pathology laboratory mentioned.': 'De aanvrager is inderdaad verbonden aan of werkt samen met het opgegeven pathologielaboratorium.',
        'The requester is allowed to submit the request.': 'De aanvrager is bevoegd deze aanvraag in te dienen.',
        'The contact person been checked and is authorised to support the request.': 'De hoofdaanvrager is bekend en ondersteunt deze aanvraag.',
        'Authentification and agreement request': 'Authentificatie en instemming aanvraag',
        'Form authentification and agreement request': 'Formulier authentificatie en instemming aanvraag',
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
        'popover: Informed consent': 'Upload de patiëntinformatie en een blanco informed consent formulier.',
        'Data linkage information': 'Koppelingsinformatie',
        'popover: Data linkage': 'Koppelingsinformatie.',
        'Which cohort or registry do you want to link to PALGA?': 'Geef aan met welke persoonsgegevens of welke registratie u wilt koppelen.',
        'popover: Notes for linkage with personal data':  'Bijvoorbeeld naam en website van uw eigen cohort of naam van de registratie (NKR, DICA, PHARMO, etc.).',
        'Explanation why linkage is allowed without informed consent': 'Toelichting gebruik persoonsgegevens zonder toestemming (informed consent)',
        'popover: Reason using personal data': 'Geef aan waarom u denkt dat informed consent niet nodig is.',
        'Notes': 'Notities',
        'Add note': 'Notitie toevoegen',
        'Please add a note.': 'Voeg een notitie toe.',
        'Edit request': 'Aanvraag bewerken',
        'Pathologist email': 'E-mail patholoog',
        'General details': 'Algemene details',
        'Review by PALGA privacy commitee': 'Toetsing door PALGA privacycommissie (PPC)',
        'This request was sent by email to the privacy committee.': 'Deze aanvraag is schriftelijk voorgelegd aan de PPC',
        'Review PPC': 'Beoordeling PPC',
        'Review result': 'Oordeel PPC',
        'Explanation for PPC': 'Toelichting voor PPC',
        'Summary review process': 'Samenvatting schriftelijke procedure',
        'Final checks before data delivery': 'Laatste controles voor datalevering',
        'Approved by scientific council.': 'De wetenschappelijke raad heeft deze aanvraag positief beoordeeld.',
        'Approved by privacy committee.': 'De privacycommissie heeft deze aanvraag positief beoordeeld.',
        'Finish': 'Afronden',
        'Finish submission process': 'Aanvraagprocedure afronden',
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

        'Review by the scientific council': 'Beoordeling door de wetenschappelijke raad (WR)',
        'Review_short': 'Beoordeling',
        'Discussion': 'Discussie',
        'Update comment': 'Opmerking bewerken',
        'Add comment': 'Opmerking plaatsen',
        'Send notification to the requester (CC hub users, if applicable).': 'Notificatie versturen naar de aanvrager (CC hub gebruikers, indien van toepassing)',
        'Send notification to the lab (CC hub users, if applicable).': 'Notificatie versturen naar het lab (CC hub gebruikers, indien van toepassing).',
        'Send notification to the requester.' : 'Notificatie versturen naar de aanvrager.',
        'Notification sent.': 'Notificatie verstuurd.',
        'popover: Upload file': 'Upload alle relevante bijlagen.',
        'popover: Upload medical ethical committee approval': 'Upload brief van medisch-ethische toetsingscommissie.',
        'Close request': 'Aanvraag afsluiten',
        'Compose mail to members': 'Schrijf e-mail aan leden',
        'Compose mail to requester': 'E-mail aanvrager',
        'Compose reopen email': 'Meld heropenen aanvraag',
        'Compose rejection mail': 'Opstellen afwijzings-mail',
        
        'Finish submission process, skip scientific council': 'Aanvraagprocedure afronden, sla wetenschappelijke raad over',
        'The scientific council and privacy committee have not been involved in this request.': 'De wetenschappelijke raad en PPC zijn niet betrokken bij deze aanvraag.',
        'Are you sure you want to finish the submission process and skip the scientific council for this request?': 'Weet u zeker dat u de aanvraagprocedure wilt afronden en dit verzoek niet naar de wetenschappelijk raad en PPC wilt sturen?',


        'ppc_handled_according_mandate': 'Afgehandeld conform mandaat PPC',
        'ppc_handled_according_mandate_explanation': 'Afgehandeld conform mandaat PPC, zie toelichting',
        'ppc_approved_written_procedure': 'Goedgekeurd na schriftelijke procedure',
        'ppc_discuss': 'Bespreken in vergadering',
        'ppc_rationale_exploratory_request': 'Oriënterende aanvraag, geen oordeel vereist',
        'ppc_local_request': 'Lokale aanvraag PA-materiaal, geen oordeel vereist',

        'Excerpt list': 'Excerptlijst',
        'Upload excerpt list': 'Excerptlijst uploaden',
        'Select all excerpts and continue': 'Alle excerpten selecteren en doorgaan',
        'Excerpt selection': 'Excerptselectie',
        'Download selection': 'Selectie downloaden',
        'Approve selection': 'Selectie goedkeuren',
        'Reject selection': 'Selectie afwijzen',

        'Other data files': 'Overige databestanden',
        'No data files.': 'Geen databestanden.',
        'Upload data files': 'Databestanden uploaden',
        'The file with PALGA data is ready for download.': 'Het bestand met PALGA-gegevens staat voor u klaar.',

        'Request number': 'Aanvraagnummer',
        'Additional requests': 'Aanvullende verzoeken',
        'Create additional request': 'Aanvullend verzoek aanmaken',
        'Are you sure you want to create an additional request?': 'Weet je zeker dat je een aanvullend verzoek wilt aanmaken?',
        'No additional requests.': 'Geen aanvullende verzoeken.',
        'Date submitted': 'Datum ingediend',
        'Date created': 'Datum aangemaakt',

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
        'User account blocked because of too many failed login attempts. Please retry in an hour.': 'Account geblokeerd vanwege te veel verkeerde inlogpogingen. Probeer het aub over een uur opnieuw.',
        'Email address (lower case)': 'E-mailadres (kleine letters)',

        /* ========= */
        /* Main page */
        /* ========= */
    
        Language: 'Taal',
        English: 'Engels',
        Dutch: 'Nederlands',
        Task: 'Taak',
        Tasks: 'Taken',
        Registration: 'Registreren',
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
        'Input value is too long (max. ? characters)': 'Invoerwaarde is te lang (max. {{max}} tekens)',
        'Pathology laboratory': 'Pathologielaboratorium',
        'Select laboratory': 'Selecteer laboratorium',
        'popover: Pathology laboratory': 'Selecteer het pathologielaboratorium waar u werkzaam bent of waarmee u samenwerkt.',

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
        'You are only allowed to edit your telephone number and specialism. If you would like to change other information please contact PALGA.': 
            'U kunt alleen uw telefoonnummer en uw specialisme aanpassen. Indien u andere informatie wilt wijzigen, neem dan contact op met PALGA.',
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
        'If an account with email address ? exists, an email with instructions on how to reset your password has been sent.': 'Als een gebruiker bestaat met emailadres {{email}}, dan hebben we een email gestuurd met instructies om uw wachtwoord te resetten.',
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
        'Claimed by': 'Geclaimd door',
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
        'Filter by pathologist': 'Filteren op patholoog',
        'Filter by linkage': 'Filteren op koppeling',
        'Filter by sequence number': 'Filteren op volgnummer',
        'Filter by PALGA patiëntnr': 'Filteren op PALGA patiëntnr',
        'Filter by PALGA excerptnr': 'Filteren op PALGA excerptnr',
        'Filter by PALGA excerptid': 'Filteren op PALGA excerptid',
        'Filter by review': 'Filteren op oordeel',
        'Filter by hub assistance': 'Filteren op inzet hubmedewerker',
        'Date assigned': 'Toegekend op',
        'My review': 'Mijn oordeel',

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
        'Lab request status': 'Status labverzoek',
        'Result': 'Resultaat',
        'REPORTS_ONLY': 'Verzoek om PA-rapporten en/of klinische gegevens afgerond',
        'RETURNED': 'PA-materiaal is retour ontvangen',
        'NONE': 'Geen',
        'Packing list': 'Pakbon',
        'Download PA numbers': 'Download PA-nummers',
        'Approve': 'Goedkeuren',
        'Request details': 'Aanvraagdetails',
        'Request ID': 'Aanvraag ID',
        'Requester lab': 'Lab aanvrager',
        'Sending lab': 'Versturend lab',
        'Lab request code': 'Labverzoekcode',
        'Are you sure you want to reject the lab request?': 'Weet u zeker dat u het labverzoek wilt afwijzen?',
        'Do you want to approve this request to your laboratory?': 'Wilt u dit verzoek aan uw laboratorium goedkeuren?',
        'Have you sent the material to the requester?': 'Heeft u het materiaal verstuurd naar de aanvrager?',
        'Did you receive the material?': 'Heeft u het materiaal ontvangen?',
        'Do you want to return the material?': 'Wilt u het materiaal terugsturen naar het laboratorium waarvan het materiaal afkomstig is?',
        'Did you receive the material in return?': 'Heeft u het materiaal retour ontvangen?',
        'Do you want to finish the request for PA reports and/or clinical data?': 'Wilt u het verzoek voor PA-verslagen en/of klinische gegevens afronden?',
        'Do you want to complete the rejected request?': 'Wilt u het afgewezen verzoek afronden?',
        'Are you sure you want to delete the PA number?': 'Weet u zeker dat u het PA-nummer wilt verwijderen?',
        'Palga request: ': 'Palga aanvraag: ',
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
        'Desired information for retrieving clinical data from treating physician was sent to the requester.':
            'Benodigde informatie voor het opvragen van klinische gegevens via de behandelaar zijn naar de aanvrager verstuurd.',
        'Desired information for retrieving clinical data from treating physician was NOT sent to the requester.':
            'Benodigde informatie voor het opvragen van klinische gegevens via de behandelaar zijn NIET naar de aanvrager verstuurd.',
        'Update clinical data status': 'Verzendstatus klinische gegevens opslaan',
        'The lab request has been approved.': 'Het labverzoek is goedgekeurd.',
        'Your laboratory is asked for PA reports, PA material and/or clinical data from the treating physician.' : 'Een onderzoeker vraagt uw laboratorium om PA-verslagen, PA-materiaal en/of klinische gegevens via de behandelaar.',
        'Select ‘Approve’ to accept or ‘Reject’ to decline this request.' : 'Als u instemt met het verzoek, kies dan ‘Goedkeuren’ anders ‘Afwijzen.’',
        'Send materials': 'Materiaal versturen',
        'Materials are missing.': 'Materiaal ontbreekt.',
        'PA material returned': 'PA-materiaal retour ontvangen',
        'PA material received': 'PA-materiaal ontvangen',
        'PA material has been received.': 'PA-materiaal is ontvangen.',
        'PA material has been received by the requester.': 'PA-materiaal is ontvangen door de aanvrager.',
        'Return materials': 'Materiaal retour sturen',
        'Missing materials': 'Ontbrekend materiaal',
        'PA material has been sent.': 'PA-materiaal is verstuurd.',
        'PA material has been returned.': 'PA-materiaal is teruggestuurd.',
        'The lab request has been rejected.': 'Het labverzoek is afgewezen.',
        'Reject date': 'Datum afwijzing',
        'Reject reason': 'Reden afwijzing',
        'Recall': 'Terugroepen',
        'Compose recall mail': 'Terugroepmail opstellen',
        'Address pathology lab. requester': 'Adres pathologie lab. aanvrager',
        'Return address': 'Retouradres',
        'Hospital': 'Ziekenhuis',
        'Lab request rejected, request number ': 'Labverzoek afgewezen, aanvraagnummer ',
        'Lab request rejected.%0AReject reason: ': 'Labverzoek afgewezen.%0AReden voor afwijzing: ',

        /* ===================== */
        /* User overview */
        /* ===================== */
        'Add user': 'Gebruiker toevoegen',
        'Name': 'Naam',
        'Email': 'E-mail',
        'Email address': 'E-mailadres',
        'Email format is invalid': 'E-mailadres heeft niet het juiste formaat',
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

        'Approved, waiting for data': 'Goedgekeurd, wachten op data',
        'Data delivered': 'Data geleverd',
        'Data delivered, select excerpts': 'Data geleverd, excerptselectie',

        'Download ...': '{{filename}} downloaden',
        'Uploading ?' : 'Bezig met uploaden van {{filename}}',
        'Done uploading file ?' : 'Klaar met uploaden van {{filename}}',
        'Done uploading file ?. The attachment has been saved.' : 'Klaar met uploaden van {{filename}}. De bijlage is opgeslagen.',
        'Successfully added filename? (type?).' : 'Bestand {{filename}} toegevoegd ({{type}}).',
        'Upload success': 'Uploaden gelukt',
        'Upload failed': 'Uploaden niet gelukt',
        'Failed to upload filename?.': 'Het uploaden van {{filename}} is niet gelukt.',
        'filetype_agreement': 'Formulier authentificatie en instemming aanvraag',
        'filetype_attachment': 'Bijlage',
        'filetype_MEC approval': 'Brief van medisch-ethische toetsingscommissie',
        'filetype_excerpt list': 'Excerptlijst',
        'filetype_excerpt selection': 'Excerptselectie',
        'filetype_data': 'Databestand',
        'Undid approval previously approved lab request': 'Goedkeuring geannuleerd voor eerder goedgekeurd labverzoek',
        'Undid rejection previously rejected lab request': 'Afwijzing geannuleerd voor eerder afgewezen labverzoek',

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
        'Enable hub assistance.': 'Hubmedewerker inzetten.',
        'Would you like hub assistance for this lab request?': 'Wilt u assistentie van de hubmedewerker voor dit labverzoek?',
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
        'Return date': 'Datum retour',
        'Undo approval': 'Goedkeuring annuleren',
        'Are you sure you want to undo approval for this previously approved lab request?': 'Weet u zeker dat u de goedkeuring wilt annuleren?',

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

        'mail_recall': '?subject=Met betrekking tot {{labRequest.labRequestCode}}, terugroepen sample {{pathology.paNumber}} &body=Beste onderzoeker,%0D%0DZou je het volgende PA-nummer z.s.m willen terugsturen. Deze hebben wij nodig voor de patiëntenzorg.%0D%0DAanvraag {{pathology.labRequestCode}}%0DPA nummer: {{pathology.paNumber}}; {{pathology.palgaPatientNr}}%0D%0DAlvast bedankt.',
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
