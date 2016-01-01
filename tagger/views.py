from django.shortcuts import render
from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver
from rest_framework.authtoken.models import Token
from rest_framework.authentication import TokenAuthentication
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.authtoken.views import ObtainAuthToken
from django.contrib.auth.models import User
from rest_framework.response import Response
#from simple_email_confirmation import send_email
from tagger.models import *
from django.db import IntegrityError
from rest_framework import status
import pdb
from datetime import datetime,timedelta
from .serializers import *
import pytz
from rest_framework import exceptions

from .models import *
from rest_framework import viewsets
from django.contrib.auth import get_user_model
from rest_framework.decorators import list_route




@receiver(post_save,sender=settings.AUTH_USER_MODEL)
def create_auth_token(sender, instance=None, created=False, **kwargs):
    if created:
        Token.objects.create(user=instance)










class ExpiringTokenAuthentication(TokenAuthentication):
    def has_permission(self, request,view):

        key = request.auth
        try:
            token = self.model.objects.get(key=key)
        except self.model.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid token')

        if not token.user.is_active:
            raise exceptions.AuthenticationFailed('User inactive or deleted')

    # This is required for the time comparison
        utc_now = datetime.utcnow()
        utc_now = utc_now.replace(tzinfo=pytz.utc)

        if token.created < utc_now - timedelta(hours=24):
            token.delete()
            raise exceptions.AuthenticationFailed('Token has expired')

        return token.user, token


class UserViewSet(viewsets.ModelViewSet):
        serializer_class = ProfileSerializer
        authentication_classes = (TokenAuthentication,)
        permission_classes = (ExpiringTokenAuthentication,)

        @list_route(methods=['post'])
        def logout(self,request,pk=None):
            request.user.user_token.delete()
            return Response({'Status':'Success'})

        @list_route(methods=['post'])
        def change_password(self,request,pk=None):

            data = request.data
            user = request.user
            psw = PasswordSerializer(data=request.data)
            if not psw.is_valid() or not psw.valid(user):
                return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

            user.set_password(data['new_password'])
            user.save()
            return Response({'Status':'Success'})
        @list_route(methods=['post'])
        def change_username(self,request,pk=None):
            data = request.data
            user = request.user

            user.username =data['new_username']
            user.save()

            return Response({'Status':'Success'})

        @list_route(methods=['post'])
        def change_email(self,request,pk=None):
            data = request.data
            user = request.user
            user.email =data['new_email']
            user.save()
            return Response({'Status':'Success'})

        @list_route(methods=['post'])
        def change_name(self,request,pk=None):
            data= request.data
            user = request.user
            user.first_name=data['new_name']
            user.save()
            return Response({'Status':'Success'})



class EventViewSet(viewsets.ModelViewSet):
    authentication_classes = (TokenAuthentication,)
    permission_classes = (ExpiringTokenAuthentication,)

    def create(self,request):
        pdb.set_trace()
        event=EventSerializer(data={'event_name':request['event_name'],'creator':request.user})
        ev_Ser = EventSerializer(event)
        return Response(ev_Ser.data)






class ObtainExpiringAuthToken(ObtainAuthToken):
    def post(self, request):
        serializer = self.serializer_class(data=request.data)

        if serializer.is_valid():
            token, created =  Token.objects.get_or_create(user=serializer.validated_data['user'])

            if not created:
                # update the created time of the token to keep it valid
                token.created = datetime.utcnow()
                token.save()

            User = get_user_model()
            user =User.objects.get(username=serializer.validated_data['user'])
            user_ser = ProfileSerializer(user)

            return Response({'user':user_ser.data,'token':token.key})
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
'''
class EmailConfirmation(APIView):

    def post(self,request):
        #serializer =
'''

class Signup(APIView):

    def post(self, request, **kwargs):

        user_name = request.data['user']
        password = request.data['password']
        name =  request.data['name']
        email = request.data['email']
        try:
            User  = get_user_model()
            user = User.objects.create_user(user_name,email,password)
            user.first_name = name

        except IntegrityError:
            return Response({"Status":"Exists"})

        #send_email(email, 'Use %s to confirm your email' % user.confirmation_key)

        return Response({'Status':'Success'})
