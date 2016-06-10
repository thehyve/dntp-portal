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
        'An email has been sent to ? with instructions on how to reset your password.': 'An email has been sent to {{email}} with instructions on how to reset your password.',
        'Registration completed. An email with an activation link was sent to ?.': 'Registration completed. An email with an activation link was sent to {{email}}.',
        'Postal code': 'Zip code',
        'Available' : 'Available',
        'Not Available' : 'Not Available',
        'Request type': 'Required data and/or materials',

        /* ========= */
        /* Request */
        /* ========= */
        'popover: Principal investigator': 'Principal investigator of this research project.',
        'popover: Pathologist': 'Pathologist involved in this research project. ',
        'popover: Pathologist email' : 'Email address of the pathologist.',
        'popover: Address': 'Address where the invoice should be sent to.',
        'popover: E-mail': 'Email billing department.',
        'popover: Charge number': 'Account where the project can be booked.',
        'popover: Grant provider': 'Grant provider.',
        'popover: Research number': 'Grant number.',
        'Running title': 'Title',
        'popover: Title' : 'Title of the research project.',
        'popover: Background': 'Project background.',
        'popover: Research question': 'Research question(s).',
        'popover: Hypothesis': 'Hypothesis.',
        'popover: Methods': 'Methods.',
        'popover: Search criteria': 'Search criteria.',
        'popover: Study period': 'Study period.',
        'popover: Laboratory techniques': 'Used techniques (e.g., germline mutation analysis, moleculair techniques, immuno histochemistry, tissue micro array). How many patients do you need (at least) to answer this research question?',
        'popover: Postal code': 'Zip code.',
        'popover: City': 'City',
        'popover: Telephone': 'Telephone number.',
        'popover: Previous contact' : 'Did you contact PALGA previously about the current request?',
        'popover: Description of previous contact': 'With whom did you speak and what did you discuss?',
        'popover: Request type': 'Required data and/or materials.',
        'popover: Data linkage' : 'Data linkage information.',
        'popover: Notes for linkage with personal data' : 'Mention the name and website of your cohort or the name of the registry you want to link to PALGA.',
        'popover: Reason using personal data': 'Explain why linkage is allowed without informed consent.',
        'popover: Upload file' : 'Upload file.',
        'popover: Upload medical ethical committee approval' : 'Upload letter of medical research ethics committee.',
        'popover: Informed consent': 'Informed consent (IC).',

        'Are you sure you want to delete file ?' : 'Are you sure you want to delete file {{name}}?',
        'Maximum file size ? MB.': 'Maximum file size {{mb_max}} MB.',
        'File too large': 'File too large ({{mb_size}} MB). Maximum size is {{mb_max}} MB.',
        'Are you sure you want to delete request ?' : 'Are you sure you want to delete request {{id}}?',
        'Edit user ...' : 'Edit user {{username}}',
        'Download ...' : 'Download {{filename}}',
        'Uploading ?' : 'Uploading {{filename}}',
        'Done uploading file ?' : 'Done uploading file {{filename}}',

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

        'ppc_handled_according_mandate': 'Handled according to mandate PPC',
        'ppc_handled_according_mandate_explanation': 'Handled according to mandate PPC, see explanation',
        'ppc_approved_written_procedure': 'Approved after written procedure',
        'ppc_discuss': 'Discuss in meeting',
        'ppc_rationale_exploratory_request': 'Exploratory request, no assessment required',

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


        'A researcher has requested data and/or materials from your lab.' : 'A researcher has requested data and/or materials from your lab.',
        'Select "Approve" to accept or "Reject" to decline this request.' : 'Select "Approve" to accept or "Reject" to decline this request',

        /* ===================== */
        /* My Lab */
        /* ===================== */


        /* ===================== */
        /* Specialism */
        /* ===================== */
        'Maag-darm-lever-ziekten':'Gastrointestinal-liver-disease',
        'Gynaecologie':'Gynecology',
        'Dermatologie':'Dermatology',
        'Medische Oncologie':'Medical Oncology',
        'Interne geneeskunde':'Internal Medicine',
        'Radiologie':'Radiology',
        'Radiotherapie':'Radiotherapy',
        'Chirurgie':'Surgery (Chirurgie)',
        'Hematologie':'Haematology',
        'Keel-neus-oor':'Throat-nose-ear',
        'Heelkunde':'Surgery (Heelkunde)',
        'Epidemiologie':'Epidemiologie',
        'Eerstelijnsgeneeskunde':'Primary care',
        'Cardiologie':'Cardiology',
        'Pathologie':'Pathology',
        'Longziekten':'Lung Disease',
        'Urologie':'Urology',
        'Neurologie':'Neurology',
        'Endocrinologie':'Endocrinology',
        '(Other)':'(Other)',
        '(Please select a specialism)' : '(Please select a specialism)',
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

