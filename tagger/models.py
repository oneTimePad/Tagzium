from django.db import models
from django.conf import settings
from django.contrib.auth.models import AbstractUser
from rest_framework.authtoken.models import Token
from django.core.files.storage import FileSystemStorage

STORAGE = '/var/www/html/PHOTOS/'
fs = FileSystemStorage(location=STORAGE)

class Profile(AbstractUser):

    

    @property
    def user_token(self):
        return Token.objects.get(user=self)

class Event(models.Model):
    event_name = models.CharField(max_length=200)
    creator = models.ForeignKey('Profile',related_name="creator")
    users = models.ManyToManyField(Profile,related_name='events_in')

class Picture(models.Model):
    photo = models.ImageField(storage=fs,default=0)
    event = models.ForeignKey('Event',default=0)
    users = models.ManyToManyField(Profile,related_name='pictures_in',default=0)
