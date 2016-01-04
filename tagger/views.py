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
from django.core.cache import cache





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


@receiver(post_save,sender=Event)
def on_recent_event(sender, instance=None, created=False, **kwargs):

    if not cache.has_key(instance.creator.username+'_recent_events'):
        recent_events = []
        recent_events.append(instance)
        cache.set(instance.creator.username+'_recent_events',recent_events)
    else:
        recent_events = cache.get(instance.creator.username+'_recent_events')
        if instance in recent_events:
            recent_events.remove(instance)
        recent_events.append(instance)
        cache.delete(instance.creator.username+'_recent_events')
        cache.put(instance+'_recent_events',recent_events)


class EventViewSet(viewsets.ModelViewSet):
    authentication_classes = (TokenAuthentication,)
    permission_classes = (ExpiringTokenAuthentication,)
    serializer_class = EventSerializer
    @list_route(methods=['post'])
    def creates(self,request):

        input = {'event_name':request.data['event_name'],'creator':request.user.pk}
        event=EventSerializer(data=input)
        if not event.is_valid():
            return Response(event.errors,status=status.HTTP_400_BAD_REQUEST)
        event = event.create()
        event.save()

        return Response({'Status':'Success'})
    @list_route(methods=['post'])
    def retrieve_users(self,request):


        if len(request.user.creator.all()) == 0:
            return Response('no_events',status=status.HTTP_400_BAD_REQUEST)

        if cache.has_key('return_all'):
            query_ser = UserEventSerializer(request.user.creator.all(),many=True)
            cache.delete('return_all')
            return Response(query_ser.data)

        if not cache.has_key(request.user.username+'_recent_events'):
            return Response('no_change',status=status.HTTP_400_BAD_REQUEST)
        recent_events = cache.get(request.user.username+'_recent_events')
        query_ser = UserEventSerializer(recent_events,many=True)
        cache.delete(request.user.username+'_recent_events')

        return Response(query_ser.data)








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
            cache.set("return_all",1)
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
            user.save()
        except IntegrityError:
            return Response({"Status":"Exists"})

        #send_email(email, 'Use %s to confirm your email' % user.confirmation_key)

        return Response({'Status':'Success'})
