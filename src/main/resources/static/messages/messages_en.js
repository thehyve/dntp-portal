/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(hasModuleExports, hasWindow) {
    'use strict';

    var _messages_en = {
        /* ========= */
        /* Global */
        /* ========= */
        'role_palga': 'PALGA',
        'role_requester': 'Requester',
        'role_lab_user': 'Lab user',
        'role_hub_user': 'Hub user',
        'role_scientific_council': 'Scientific council',

        'An email has been sent to ? with instructions on how to reset your password.': 'An email has been sent to {{email}} with instructions on how to reset your password.',
        'If an account with email address ? exists, an email with instructions on how to reset your password has been sent.': 'If an account with email address {{email}} exists, an email with instructions on how to reset your password has been sent.',
        'Registration completed. An email with an activation link was sent to ?.': 'Registration completed. An email with an activation link was sent to {{email}}.',
        'Postal code': 'Zip code',
        'Available' : 'Available',
        'Not Available' : 'Not Available',
        'Request type': 'Required data and/or materials',
        'Input value is too long (max. ? characters)': 'Input value is too long (max. {{max}} characters)',
        'User has been added. A password reset mail has been sent to ?.':
            'User has been added. A password reset mail has been sent to {{email}}.',
        'Are you sure you want to delete user ? ?': 'Are you sure you want to delete user {{name}}?',

        /* ========= */
        /* Request */
        /* ========= */
        'popover: Principal investigator': 'Principal investigator of this research project.',
        'popover: Pathologist': 'Pathologist involved in this research project. ',
        'popover: Address': 'Address where the invoice should be sent to.',
        'popover: E-mail': 'Email billing department.',
        'popover: Charge number': 'Account where the project can be booked.',
        'popover: Grant provider': 'Grant provider.',
        'popover: Research number': 'Grant number.',
        'Running title': 'Title',
        'popover: Title' : 'Title of the research project (max. 100 characters).',
        'popover: Background': 'Project background.',
        'popover: Research question': 'Research question(s).',
        'popover: Hypothesis': 'Hypothesis.',
        'popover: Methods': 'Methods.',
        'popover: Search criteria': 'Search criteria.',
        'popover: Biobank request': 'Ask your biobank or pathology department if a biobank request number is required',
        'popover: Laboratory techniques': 'Used techniques (e.g., germline mutation analysis, moleculair techniques, immuno histochemistry, tissue micro array). How many patients do you need (at least) to answer this research question?',
        'popover: Postal code': 'Zip code.',
        'popover: City': 'City',
        'popover: Description of previous contact': 'With whom did you speak and what did you discuss?',
        'popover: Request type': 'Required data and/or materials.',
        'popover: Data linkage' : 'Data linkage information.',
        'popover: Notes for linkage with personal data' : 'Mention the name and website of your cohort or the name of the registry you want to link to PALGA.',
        'popover: Reason using personal data': 'Explain why linkage is allowed without informed consent.',
        'popover: Upload file' : 'Upload file.',
        'popover: Upload medical ethical committee approval' : 'Upload letter of medical research ethics committee.',
        'popover: Informed consent': 'Upload patient information and blank informed consent form.',
        'popover: Pathology laboratory': 'Select the pathology laboratory where you work or with which you collaborate.',
        'info: informed consent upload': 'Upload <em>blank</em> informed consent form',
        'No informed consent form uploaded.': 'No informed consent form uploaded.',

        'PA reports': "<em>Anonymous</em> PA reports",
        'Additional request for request ?': 'Additional request for {{requestNumber}}',
        'Are you sure you want to delete file ?' : 'Are you sure you want to delete file {{name}}?',
        'Maximum file size ? MB.': 'Maximum file size {{mb_max}} MB.',
        'File too large': 'File too large ({{mb_size}} MB). Maximum size is {{mb_max}} MB.',
        'Are you sure you want to delete request ?' : 'Are you sure you want to delete request {{id}}?',
        'Edit user ...' : 'Edit user {{username}}',
        'Download ...' : 'Download {{filename}}',
        'Uploading ?' : 'Uploading {{filename}}',
        'Done uploading file ?' : 'Done uploading file {{filename}}',
        'Done uploading file ?. The attachment has been saved.' : 'Done uploading file {{filename}}. The attachment has been saved.',
        'The excerpt list exceeds the limit of ?. The requester cannot use the excerpt selection interface for this request.':
            'The excerpt list exceeds the limit of {{limit}}. The requester cannot use the excerpt selection interface for this request.',
        'Successfully added filename? (type?).' : 'Successfully added {{filename}} ({{type}}).',
        'Failed to upload filename?.': 'Failed to upload {{filename}}.',
        'filetype_agreement': 'Form authentification and agreement request',
        'filetype_attachment': 'Attachment',
        'filetype_MEC approval': 'Letter of medical research ethics committee',
        'filetype_excerpt list': 'Excerpt list',
        'filetype_excerpt selection': 'Excerpt selection',
        'filetype_data': 'Data',
        'Palga request: ': 'Palga request: ',
        'Address pathology lab. requester': 'Address pathology lab. requester',
        'Review_short': 'Review',

        'ACCEPTED' : 'Accepted',
        'REJECTED' : 'Rejected',

        'Request_Type_1' : 'Numbers only, exploratory request',
        'Request_Type_2' : 'Excerpts only',
        'Request_Type_3' : 'Excerpts and full PA reports',
        'Request_Type_4' : 'Excerpts and PA material',
        'Request_Type_5' : 'Excerpts, PA reports and PA material',
        'Request_Type_6' : 'Full PA reports only (no excerpts)',
        'Request_Type_7' : 'PA material only',

        'ppc_handled_according_mandate': 'Approved according to mandate PPC',
        'ppc_handled_according_mandate_explanation': 'Approved according to mandate PPC, see explanation',
        'ppc_approved_written_procedure': 'Approved after written procedure',
        'ppc_discuss': 'Review in half year meeting',
        'ppc_rationale_exploratory_request': 'Exploratory request, no review required',
        'ppc_local_request': 'Local request for PA material, no review required',

        /* ===================== */
        /* Agreement overview    */
        /* ===================== */
        'popover:Markdown Help': 'Markdown Help',
        'Agreement form template': 'Agreement form template',

        /* ===================== */
        /* Status */
        /* ===================== */
        'Open': 'Open, not yet submitted',
        'Review': 'Received by PALGA advisor',
        'Approval': 'Waiting for approval',
        'DataDelivery': 'Data delivery and selection',
        'SelectionReview': 'Selection received',
        'LabRequest': 'Lab request',
        'Waiting for lab approval': 'Under review by lab',
        'Approved': 'Approved',
        'Sending': 'Materials sent',
        'Received': 'Received',
        'Returning': 'Sent in return',
        'Returned': 'Returned',
        'Completed': 'Completed',
        'Closed': 'Closed',

        'REPORTS_ONLY': 'Request for PA reports and/or clinical data completed',
        'RETURNED': 'PA material has been returned',
        'NONE': 'None',

        'A researcher has requested data and/or materials from your lab.' : 'A researcher has requested data and/or materials from your lab.',
        'Select "Approve" to accept or "Reject" to decline this request.' : 'Select "Approve" to accept or "Reject" to decline this request',

        /* ===================== */
        /* My Lab */
        /* ===================== */

        /* ===================== */
        /* Packing list */
        /* ===================== */
        'Delivery address':'Delivery address',
        'Billing address': 'Billing address',
        'Return address': 'Return address',
        'Biobank request number': 'Biobank request number',
        'PALGA request': 'PALGA request',
        'Hospital': 'Hospital',
        'Hereby you receive this material for a scientific study. We request you notify': 'Hereby you receive this material for a scientific study. We request you notify',

        /* ===================== */
        /* Specialism */
        /* ===================== */
        'Maag-darm-lever-ziekten':'Gastroenterology',
        'Gynaecologie':'Gynaecology',
        'Dermatologie':'Dermatology',
        'Medische Oncologie':'Medical Oncology',
        'Interne geneeskunde':'Internal Medicine',
        'Radiologie':'Radiology',
        'Radiotherapie':'Radiotherapy',
        'Hematologie':'Haematology',
        'Keel-neus-oor':'Throat-nose-ear',
        'Heelkunde':'Surgery',
        'Epidemiologie':'Epidemiology',
        'Eerstelijnsgeneeskunde':'Primary care',
        'Cardiologie':'Cardiology',
        'Pathologie':'Pathology',
        'Longziekten':'Lung Disease',
        'Urologie':'Urology',
        'Neurologie':'Neurology',
        'Endocrinologie':'Endocrinology',
        '(Other)':'(Other)',
        '(Please select a specialism)' : '(Please select a specialism)',

        'mail_recall': '?subject=Regarding request {{labRequest.labRequestCode}}, recall sample {{pathology.paNumber}}&body=Dear researcher,%0D%0DPlease return the following PA-number asap. We need this for further patient diagnostic.%0D%0DPALGA aanvraag {{pathology.labRequestCode}}%0D%0DPA number {{pathology.paNumber}}; {{pathology.palgaPatientNr}}%0D%0DBest regards.',
        null: null
    };

    if (hasWindow) {
        var messages = window.messages || (window.messages = {});
        messages.en = _messages_en;
    }

    if (hasModuleExports) {
        module.exports = _messages_en;
    }

})(typeof module !== 'undefined' && module.exports, typeof window !== 'undefined');

