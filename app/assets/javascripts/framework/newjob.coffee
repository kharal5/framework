###
 Copyright 2015 org.NLP4L

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
###

$ ->
  check_input = () ->
    name = $('#name').val()
    if (name.length > 0)
      $('#save-button').prop('disabled', false)
    else
      $('#save-button').prop('disabled', true)

  $('#name').keyup ->
    name = $('#name').val()
    check_input()

  $('#config').change ->
    files = $('#config')[0].files
    if (files.length == 1)
      $('#upload-button').prop('disabled', false)
    else
      $('#upload-button').prop('disabled', true)

  $('#save-button').click ->
    name = $('#name').val()
    config = $('#configdata').val()
    $.ajax
      url: '/job/info',
      type: 'POST',
      contentType: 'text/json',
      data: JSON.stringify({"name": name, "config":config}),
      success: (data, textStatus, jqXHR) ->
        jump = '/dashboard/job/' + data.jobId
        location.replace(jump)

  $('#upload').click ->
    action = '/job/config'
    $('#uploadform').attr('action', action)
    $('#uploadform').attr('method', 'POST')
    $('#uploadform').submit()